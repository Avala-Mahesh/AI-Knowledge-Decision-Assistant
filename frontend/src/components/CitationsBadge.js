import React from 'react';

const CitationsBadge = ({ citations }) => {
  if (!citations || citations.length === 0) return null;

  return (
    <div className="mt-2 pt-2 border-top">
      <div className="text-muted small fw-bold mb-1 d-flex align-items-center gap-1">
        <i className="bi bi-journal-bookmark-fill text-primary"></i> Grounded Document References:
      </div>
      <div className="d-flex flex-wrap gap-2">
        {citations.map((c, idx) => (
          <span
            key={idx}
            className="badge bg-light text-dark border p-2 d-inline-flex align-items-center gap-1 shadow-xs"
            title={c.snippet || ''}
          >
            <i className="bi bi-file-earmark-pdf-fill text-danger"></i>
            <span className="fw-semibold">{c.documentName}</span>
            <span className="badge bg-secondary text-white rounded-pill ms-1">Page {c.pageNumber}</span>
          </span>
        ))}
      </div>
    </div>
  );
};

export default CitationsBadge;
