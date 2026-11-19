import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { assistantApi } from '../api/assistantApi';

const Dashboard = ({ user }) => {
  const [stats, setStats] = useState(null);
  const [recentQuestions, setRecentQuestions] = useState([]);
  const [copied, setCopied] = useState(false);
  const [error, setError] = useState(null);

  const fetchDashboardData = async () => {
    try {
      const [statsData, questionsData] = await Promise.all([
        assistantApi.getStats(),
        assistantApi.getRecentQuestions(8),
      ]);
      setStats(statsData);
      setRecentQuestions(questionsData);
    } catch (err) {
      setError('Failed to load dashboard metrics: ' + (err.response?.data?.message || err.message));
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="container py-4">
      {/* Top Banner */}
      <div className="d-flex flex-wrap justify-content-between align-items-center mb-4 pb-3 border-bottom gap-3">
        <div>
          <h2 className="fw-bold mb-1">
            <i className="bi bi-building text-primary me-2"></i>
            {user?.organizationName || 'Organization Dashboard'}
          </h2>
          <p className="text-muted small mb-0">
            Tenant ID: <code>org-{user?.organizationId}</code> | Multi-tenant SaaS Knowledge &amp; Decision Assistant
          </p>
        </div>

        <div className="d-flex gap-2">
          <Link to="/documents" className="btn btn-outline-primary btn-sm d-flex align-items-center gap-1">
            <i className="bi bi-file-earmark-arrow-up"></i> Manage PDFs
          </Link>
          <Link to="/assistant-config" className="btn btn-primary btn-sm d-flex align-items-center gap-1 shadow-sm">
            <i className="bi bi-gear-fill"></i> Assistant &amp; Embed
          </Link>
        </div>
      </div>

      {error && (
        <div className="alert alert-danger py-2 small d-flex align-items-center gap-2 mb-4" role="alert">
          <i className="bi bi-exclamation-octagon-fill"></i>
          <span>{error}</span>
        </div>
      )}

      {/* Metrics Row */}
      <div className="row g-3 mb-4">
        {/* Knowledge Base Capacity */}
        <div className="col-sm-6 col-lg-3">
          <div className="card h-100 border-0 shadow-sm rounded-3">
            <div className="card-body">
              <div className="d-flex justify-content-between align-items-center mb-2">
                <span className="text-muted small fw-semibold">Knowledge Base</span>
                <span className="badge bg-primary-subtle text-primary p-2 rounded-circle">
                  <i className="bi bi-file-earmark-pdf fs-5"></i>
                </span>
              </div>
              <h3 className="fw-bold mb-1">
                {stats?.totalDocuments || 0} <span className="text-muted fs-6 fw-normal">/ {stats?.maxAllowedDocuments || 10} PDFs</span>
              </h3>
              <div className="progress mt-2" style={{ height: '6px' }}>
                <div
                  className="progress-bar bg-primary"
                  style={{ width: `${((stats?.totalDocuments || 0) / 10) * 100}%` }}
                ></div>
              </div>
              <span className="text-muted extra-small mt-1 d-block">
                {stats?.totalChunks || 0} indexed knowledge chunks
              </span>
            </div>
          </div>
        </div>

        {/* Total Questions Asked */}
        <div className="col-sm-6 col-lg-3">
          <div className="card h-100 border-0 shadow-sm rounded-3">
            <div className="card-body">
              <div className="d-flex justify-content-between align-items-center mb-2">
                <span className="text-muted small fw-semibold">Total Questions</span>
                <span className="badge bg-info-subtle text-info p-2 rounded-circle">
                  <i className="bi bi-chat-dots fs-5"></i>
                </span>
              </div>
              <h3 className="fw-bold mb-1">{stats?.totalQuestionsAsked || 0}</h3>
              <span className="text-muted small">Queries processed via Pipeline</span>
            </div>
          </div>
        </div>

        {/* Successfully Answered */}
        <div className="col-sm-6 col-lg-3">
          <div className="card h-100 border-0 shadow-sm rounded-3">
            <div className="card-body">
              <div className="d-flex justify-content-between align-items-center mb-2">
                <span className="text-muted small fw-semibold">Grounded Answers</span>
                <span className="badge bg-success-subtle text-success p-2 rounded-circle">
                  <i className="bi bi-check2-circle fs-5"></i>
                </span>
              </div>
              <h3 className="fw-bold text-success mb-1">{stats?.totalQuestionsAnswered || 0}</h3>
              <span className="text-muted small">Citations &amp; System 1 Verified</span>
            </div>
          </div>
        </div>

        {/* Unable to Answer / Out-of-Scope */}
        <div className="col-sm-6 col-lg-3">
          <div className="card h-100 border-0 shadow-sm rounded-3">
            <div className="card-body">
              <div className="d-flex justify-content-between align-items-center mb-2">
                <span className="text-muted small fw-semibold">Review / Unanswered</span>
                <span className="badge bg-warning-subtle text-warning p-2 rounded-circle">
                  <i className="bi bi-shield-exclamation fs-5"></i>
                </span>
              </div>
              <h3 className="fw-bold text-warning mb-1">{stats?.totalQuestionsUnableToAnswer || 0}</h3>
              <span className="text-muted small">Out-of-scope / Unmapped knowledge</span>
            </div>
          </div>
        </div>
      </div>

      {/* Assistant Public URL Banner */}
      <div className="card border-0 bg-primary bg-opacity-10 shadow-sm rounded-3 mb-4">
        <div className="card-body p-4 d-flex flex-wrap justify-content-between align-items-center gap-3">
          <div>
            <h5 className="fw-bold text-primary mb-1">
              <i className="bi bi-link-45deg me-1"></i> Public Assistant Endpoint
            </h5>
            <p className="text-muted small mb-0">
              Visitors can query your organization's assistant directly without logging in.
            </p>
            <div className="mt-2">
              <code className="bg-white p-2 rounded border small text-dark d-inline-block">
                {stats?.assistantUrl || 'Loading...'}
              </code>
            </div>
          </div>

          <div className="d-flex gap-2">
            <button
              onClick={() => copyToClipboard(stats?.assistantUrl)}
              className="btn btn-outline-primary btn-sm d-flex align-items-center gap-1"
            >
              <i className={`bi ${copied ? 'bi-check2' : 'bi-clipboard'}`}></i>
              {copied ? 'Copied URL!' : 'Copy Assistant Link'}
            </button>
            <a
              href={stats?.assistantUrl}
              target="_blank"
              rel="noreferrer"
              className="btn btn-primary btn-sm d-flex align-items-center gap-1 shadow-sm"
            >
              <i className="bi bi-box-arrow-up-right"></i> Open Visitor Chat
            </a>
          </div>
        </div>
      </div>

      {/* Recent Activity Log with System 1 Decisions */}
      <div className="card border-0 shadow-sm rounded-3">
        <div className="card-header bg-white py-3 d-flex justify-content-between align-items-center">
          <h5 className="fw-bold mb-0">
            <i className="bi bi-clock-history text-secondary me-2"></i> Recent Assistant Questions &amp; Reasoning
          </h5>
          <button onClick={fetchDashboardData} className="btn btn-sm btn-outline-secondary">
            <i className="bi bi-arrow-clockwise"></i> Refresh
          </button>
        </div>

        <div className="card-body p-0">
          {recentQuestions.length === 0 ? (
            <div className="text-center py-5 text-muted">
              <i className="bi bi-chat-square-text fs-1 d-block mb-2 text-secondary opacity-50"></i>
              <p className="mb-1">No questions asked to this assistant yet.</p>
              <p className="small">Use the Assistant Link above or the Test Chat to ask your first question!</p>
            </div>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light small text-secondary">
                  <tr>
                    <th style={{ width: '35%' }}>Question</th>
                    <th>Type</th>
                    <th>System 1 Decision Engine</th>
                    <th>Status</th>
                    <th>Latency</th>
                    <th>Timestamp</th>
                  </tr>
                </thead>
                <tbody className="small">
                  {recentQuestions.map((q) => (
                    <tr key={q.id}>
                      <td className="fw-semibold text-dark">
                        {q.question}
                      </td>
                      <td>
                        <span className="badge bg-secondary-subtle text-secondary border">
                          {q.questionType || 'DIRECT_INFO'}
                        </span>
                      </td>
                      <td>
                        {q.decisionEngineUsed ? (
                          <span className="badge bg-info-subtle text-info-emphasis border" title={q.decisionSummary}>
                            <i className="bi bi-cpu-fill me-1"></i> Executed
                          </span>
                        ) : (
                          <span className="text-muted extra-small">Direct Retrieval</span>
                        )}
                      </td>
                      <td>
                        <span className={`badge ${q.status === 'ANSWERED' ? 'bg-success' : 'bg-warning text-dark'}`}>
                          {q.status}
                        </span>
                      </td>
                      <td className="text-muted">
                        {q.responseTimeMs != null ? `${q.responseTimeMs} ms` : '-'}
                      </td>
                      <td className="text-muted">
                        {new Date(q.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
