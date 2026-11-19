import React, { useState } from 'react';

const DecisionTraceAccordion = ({ trace }) => {
  const [expanded, setExpanded] = useState(false);

  if (!trace || !trace.decisionEngineUsed) return null;

  const decisions = trace.decisions || [];

  return (
    <div className="mt-2 pt-2 border-top">
      <button
        onClick={() => setExpanded(!expanded)}
        className="btn btn-sm btn-outline-primary d-flex align-items-center gap-2 py-1 px-2"
        style={{ fontSize: '0.8rem' }}
      >
        <i className={`bi ${expanded ? 'bi-chevron-up' : 'bi-diagram-3'}`}></i>
        <span>{expanded ? 'Hide System 1 Decision Pipeline' : 'Inspect System 1 Decision Pipeline'}</span>
        <span className="badge bg-primary text-white ms-1">
          {decisions.length} Models ({trace.totalProcessingTimeMs} ms)
        </span>
      </button>

      {expanded && (
        <div className="card mt-2 border-primary bg-light shadow-sm" style={{ fontSize: '0.85rem' }}>
          <div className="card-header bg-primary text-white py-1 px-3 d-flex justify-content-between align-items-center">
            <span className="fw-semibold">
              <i className="bi bi-cpu me-1"></i> System 1 Deterministic Reasoning Trace
            </span>
            <span className="badge bg-light text-primary">
              Parallel Execution
            </span>
          </div>

          <div className="card-body p-2">
            {trace.extractedParameters && Object.keys(trace.extractedParameters).length > 0 && (
              <div className="mb-2 p-2 bg-white rounded border">
                <span className="fw-bold text-secondary">
                  <i className="bi bi-braces me-1"></i> Extracted Parameters (NLP Decomposer):
                </span>
                <div className="d-flex flex-wrap gap-1 mt-1">
                  {Object.entries(trace.extractedParameters).map(([k, v]) => (
                    <span key={k} className="badge bg-secondary-subtle text-dark border">
                      {k}: <strong>{String(v)}</strong>
                    </span>
                  ))}
                </div>
              </div>
            )}

            <div className="d-flex flex-column gap-2">
              {decisions.map((d, i) => (
                <div key={i} className="p-2 bg-white rounded border border-start border-4 border-start-primary">
                  <div className="d-flex justify-content-between align-items-center mb-1">
                    <span className="fw-bold text-dark">
                      <span className="badge bg-dark text-white me-1">{d.decisionType}</span>
                      {d.decisionName}
                    </span>
                    <span className={`badge ${d.passed ? 'bg-success' : 'bg-danger'}`}>
                      {d.outcome} {d.score != null ? `(${d.score} pts)` : ''}
                    </span>
                  </div>

                  {d.ruleBreakdown && d.ruleBreakdown.length > 0 && (
                    <ul className="mb-0 ps-3 small text-muted">
                      {d.ruleBreakdown.map((r, rIdx) => (
                        <li key={rIdx}>{r}</li>
                      ))}
                    </ul>
                  )}
                </div>
              ))}
            </div>

            {trace.decisionSummary && (
              <div className="mt-2 p-2 bg-info-subtle text-info-emphasis rounded small">
                <strong>Combined Outcome:</strong> {trace.decisionSummary}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default DecisionTraceAccordion;
