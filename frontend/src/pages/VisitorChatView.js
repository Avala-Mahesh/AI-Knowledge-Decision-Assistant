import React, { useState, useEffect, useRef } from 'react';
import { useParams } from 'react-router-dom';
import { chatApi } from '../api/chatApi';
import CitationsBadge from '../components/CitationsBadge';
import DecisionTraceAccordion from '../components/DecisionTraceAccordion';

const VisitorChatView = () => {
  const { token } = useParams();
  const [assistantInfo, setAssistantInfo] = useState(null);
  const [messages, setMessages] = useState([]);
  const [inputQuestion, setInputQuestion] = useState('');
  const [loading, setLoading] = useState(false);
  const [initLoading, setInitLoading] = useState(true);
  const [error, setError] = useState(null);
  const messagesEndRef = useRef(null);

  // Suggested test questions based on organization
  const presetQuestionsApex = [
    "What is the priority application deadline for fall term merit scholarships?",
    "I am a 3rd year student with a 3.85 GPA and an existing athletic scholarship. Am I eligible for the Presidential Merit Award?",
    "Can academic merit awards be combined with sports scholarships?",
    "What is the recipe for chocolate chip cookies?",
  ];

  const presetQuestionsNexus = [
    "What is the policy regarding domestic flight advance booking lead times?",
    "I have an expense for a flight of $1,200 booked 18 days in advance. Is this compliant and who must approve it?",
    "What is the monthly internet reimbursement allowance for remote employees?",
    "What is the recipe for chocolate chip cookies?",
  ];

  useEffect(() => {
    const fetchInfo = async () => {
      setInitLoading(true);
      setError(null);
      try {
        const info = await chatApi.getAssistantPublicInfo(token);
        setAssistantInfo(info);
        // Initial welcome message
        setMessages([
          {
            id: 'welcome',
            sender: 'bot',
            text: `Hello! I am the AI Knowledge & Decision Assistant for **${info.organizationName}**. How can I help you today? You can ask questions about our institutional policies, criteria, or guidelines.`,
            references: [],
            pipelineTrace: null,
            timestamp: new Date(),
          },
        ]);
      } catch (err) {
        setError(
          err.response?.data?.message ||
          'Invalid or expired assistant token. Please verify the URL.'
        );
      } finally {
        setInitLoading(false);
      }
    };

    if (token) {
      fetchInfo();
    }
  }, [token]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, loading]);

  const handleSendMessage = async (textToSend) => {
    const question = textToSend || inputQuestion;
    if (!question || !question.trim() || loading) return;

    const userMsg = {
      id: Date.now().toString(),
      sender: 'user',
      text: question.trim(),
      timestamp: new Date(),
    };

    setMessages((prev) => [...prev, userMsg]);
    setInputQuestion('');
    setLoading(true);
    setError(null);

    try {
      const response = await chatApi.askQuestion(token, question.trim());
      const botMsg = {
        id: (Date.now() + 1).toString(),
        sender: 'bot',
        text: response.answer,
        references: response.references || [],
        status: response.status,
        pipelineTrace: response.pipelineTrace,
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, botMsg]);
    } catch (err) {
      const errMsg = {
        id: (Date.now() + 1).toString(),
        sender: 'bot',
        text: 'An error occurred while processing your request: ' + (err.response?.data?.message || err.message),
        references: [],
        status: 'ERROR',
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, errMsg]);
    } finally {
      setLoading(false);
    }
  };

  const isApex = assistantInfo?.organizationName?.toLowerCase().includes('apex');
  const presets = isApex ? presetQuestionsApex : presetQuestionsNexus;

  if (initLoading) {
    return (
      <div className="d-flex flex-column align-items-center justify-content-center min-vh-100 bg-light">
        <div className="spinner-border text-primary" role="status"></div>
        <p className="mt-3 text-muted small">Connecting to Organization Assistant...</p>
      </div>
    );
  }

  if (error && !assistantInfo) {
    return (
      <div className="container py-5 text-center">
        <div className="card shadow-sm border-danger mx-auto" style={{ maxWidth: '480px' }}>
          <div className="card-body p-4">
            <i className="bi bi-shield-x text-danger fs-1 mb-2"></i>
            <h4 className="fw-bold text-danger">Assistant Access Denied</h4>
            <p className="text-muted small mb-4">{error}</p>
            <a href="/login" className="btn btn-outline-secondary btn-sm">
              Go to Admin Login
            </a>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="d-flex flex-column vh-100 bg-light">
      {/* Assistant Header */}
      <header className="bg-white border-bottom py-3 px-4 shadow-xs">
        <div className="container-fluid d-flex justify-content-between align-items-center">
          <div className="d-flex align-items-center gap-3">
            <div className="p-2 bg-primary text-white rounded-circle fs-5 d-flex align-items-center justify-content-center" style={{ width: '42px', height: '42px' }}>
              <i className="bi bi-robot"></i>
            </div>
            <div>
              <h5 className="fw-bold mb-0 text-dark">{assistantInfo?.organizationName} Assistant</h5>
              <span className="badge bg-success-subtle text-success small d-inline-flex align-items-center gap-1">
                <span className="p-1 bg-success rounded-circle"></span> Online &amp; Knowledge Grounded
              </span>
            </div>
          </div>

          <div className="d-none d-md-block text-end">
            <span className="badge bg-light text-muted border font-monospace extra-small">
              Token: {token?.substring(0, 16)}...
            </span>
          </div>
        </div>
      </header>

      {/* Suggested Questions Chips */}
      <div className="bg-light border-bottom px-3 py-2">
        <div className="container-fluid d-flex align-items-center gap-2 overflow-auto text-nowrap pb-1">
          <span className="small text-secondary fw-semibold d-flex align-items-center gap-1">
            <i className="bi bi-stars text-warning"></i> Try:
          </span>
          {presets.map((q, idx) => (
            <button
              key={idx}
              onClick={() => handleSendMessage(q)}
              disabled={loading}
              className="btn btn-sm btn-white bg-white border text-secondary rounded-pill py-1 px-3 shadow-2xs text-truncate"
              style={{ maxWidth: '340px', fontSize: '0.78rem' }}
              title={q}
            >
              {q}
            </button>
          ))}
        </div>
      </div>

      {/* Chat Messages Feed */}
      <main className="flex-grow-1 overflow-auto p-3 p-md-4">
        <div className="container" style={{ maxWidth: '840px' }}>
          {messages.map((m) => (
            <div
              key={m.id}
              className={`d-flex mb-3 ${m.sender === 'user' ? 'justify-content-end' : 'justify-content-start'}`}
            >
              <div
                className={`card border-0 rounded-4 shadow-sm ${
                  m.sender === 'user'
                    ? 'bg-primary text-white'
                    : 'bg-white text-dark border'
                }`}
                style={{ maxWidth: '85%' }}
              >
                <div className="card-body p-3">
                  {m.sender === 'bot' && (
                    <div className="d-flex align-items-center gap-1 mb-2 text-primary small fw-bold">
                      <i className="bi bi-robot"></i>
                      <span>{assistantInfo?.organizationName} AI</span>
                    </div>
                  )}

                  <div
                    className="mb-0"
                    style={{
                      whiteSpace: 'pre-wrap',
                      lineHeight: '1.5',
                      fontSize: '0.92rem',
                    }}
                  >
                    {m.text}
                  </div>

                  {/* Citations if any */}
                  {m.sender === 'bot' && m.references && m.references.length > 0 && (
                    <CitationsBadge citations={m.references} />
                  )}

                  {/* System 1 Decision Reasoning Trace */}
                  {m.sender === 'bot' && m.pipelineTrace && (
                    <DecisionTraceAccordion trace={m.pipelineTrace} />
                  )}

                  <div
                    className={`extra-small mt-1 text-end ${
                      m.sender === 'user' ? 'text-white-50' : 'text-muted'
                    }`}
                  >
                    {new Date(m.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </div>
                </div>
              </div>
            </div>
          ))}

          {loading && (
            <div className="d-flex justify-content-start mb-3">
              <div className="card border-0 bg-white rounded-4 shadow-sm p-3">
                <div className="d-flex align-items-center gap-2 text-muted small">
                  <span className="spinner-grow spinner-grow-sm text-primary"></span>
                  <span>Retrieving knowledge chunks &amp; evaluating System 1 models...</span>
                </div>
              </div>
            </div>
          )}

          <div ref={messagesEndRef} />
        </div>
      </main>

      {/* Input Bar */}
      <footer className="bg-white border-top p-3 shadow-lg">
        <div className="container" style={{ maxWidth: '840px' }}>
          <form
            onSubmit={(e) => {
              e.preventDefault();
              handleSendMessage();
            }}
          >
            <div className="input-group">
              <input
                type="text"
                className="form-control form-control-lg border-primary border-opacity-25"
                placeholder="Ask any policy, eligibility, or procedural question..."
                value={inputQuestion}
                onChange={(e) => setInputQuestion(e.target.value)}
                disabled={loading}
                autoFocus
              />
              <button
                type="submit"
                disabled={!inputQuestion.trim() || loading}
                className="btn btn-primary px-4 fw-semibold d-flex align-items-center gap-2 shadow-sm"
              >
                <span>Ask</span>
                <i className="bi bi-send-fill"></i>
              </button>
            </div>
          </form>
          <div className="text-center extra-small text-muted mt-2">
            AI responses are grounded in official uploaded PDFs. System 1 deterministic decisions verify eligibility and policy rules.
          </div>
        </div>
      </footer>
    </div>
  );
};

export default VisitorChatView;
