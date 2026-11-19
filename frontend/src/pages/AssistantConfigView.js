import React, { useState, useEffect } from 'react';
import { assistantApi } from '../api/assistantApi';

const AssistantConfigView = ({ user }) => {
  const [stats, setStats] = useState(null);
  const [regenerating, setRegenerating] = useState(false);
  const [copiedUrl, setCopiedUrl] = useState(false);
  const [copiedIframe, setCopiedIframe] = useState(false);
  const [copiedJs, setCopiedJs] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  const fetchConfig = async () => {
    try {
      const data = await assistantApi.getStats();
      setStats(data);
    } catch (err) {
      setError('Failed to fetch assistant config: ' + (err.response?.data?.message || err.message));
    }
  };

  useEffect(() => {
    fetchConfig();
  }, []);

  const handleRegenerateToken = async () => {
    if (!window.confirm('Are you sure you want to regenerate your Assistant Token? Existing embedded iframes and URLs will immediately stop working!')) {
      return;
    }

    setRegenerating(true);
    setError(null);
    setSuccess(null);

    try {
      await assistantApi.regenerateToken();
      setSuccess('Assistant token regenerated successfully! New token is active.');
      fetchConfig();
    } catch (err) {
      setError('Failed to regenerate token: ' + (err.response?.data?.message || err.message));
    } finally {
      setRegenerating(false);
    }
  };

  const copyToClipboard = (text, type) => {
    navigator.clipboard.writeText(text);
    if (type === 'url') {
      setCopiedUrl(true);
      setTimeout(() => setCopiedUrl(false), 2000);
    } else if (type === 'iframe') {
      setCopiedIframe(true);
      setTimeout(() => setCopiedIframe(false), 2000);
    } else if (type === 'js') {
      setCopiedJs(true);
      setTimeout(() => setCopiedJs(false), 2000);
    }
  };

  const origin = window.location.origin;
  const assistantUrl = `${origin}/chat/${stats?.assistantToken || ''}`;
  const iframeCode = `<iframe\n  src="${assistantUrl}"\n  width="100%"\n  height="600"\n  frameborder="0"\n  style="border-radius: 12px; box-shadow: 0 4px 24px rgba(0,0,0,0.12); border: 1px solid #e5e7eb;">\n</iframe>`;
  const jsWidgetCode = `<!-- AI Knowledge Assistant Floating Widget -->\n<script\n  src="${origin}/embed.js"\n  data-assistant-token="${stats?.assistantToken || ''}">\n</script>`;

  return (
    <div className="container py-4">
      {/* Header */}
      <div className="d-flex flex-wrap justify-content-between align-items-center mb-4 pb-3 border-bottom gap-3">
        <div>
          <h2 className="fw-bold mb-1">
            <i className="bi bi-gear-wide-connected text-primary me-2"></i>
            Assistant &amp; Embed Configuration
          </h2>
          <p className="text-muted small mb-0">
            Organization: <strong>{user?.organizationName}</strong> | Secure public URL &amp; embeddable web widgets
          </p>
        </div>

        <button
          onClick={handleRegenerateToken}
          disabled={regenerating}
          className="btn btn-outline-danger btn-sm d-flex align-items-center gap-1"
        >
          {regenerating ? (
            <>
              <span className="spinner-border spinner-border-sm"></span>
              <span>Regenerating...</span>
            </>
          ) : (
            <>
              <i className="bi bi-arrow-repeat"></i>
              <span>Regenerate Token</span>
            </>
          )}
        </button>
      </div>

      {error && (
        <div className="alert alert-danger py-2 small d-flex align-items-center gap-2 mb-3" role="alert">
          <i className="bi bi-exclamation-triangle-fill"></i>
          <span>{error}</span>
        </div>
      )}

      {success && (
        <div className="alert alert-success py-2 small d-flex align-items-center gap-2 mb-3" role="alert">
          <i className="bi bi-check-circle-fill"></i>
          <span>{success}</span>
        </div>
      )}

      <div className="row g-4 mb-4">
        {/* Token & URL Card */}
        <div className="col-lg-6">
          <div className="card h-100 border-0 shadow-sm rounded-3">
            <div className="card-header bg-white py-3">
              <h5 className="fw-bold mb-0">
                <i className="bi bi-key-fill text-warning me-2"></i>
                Assistant Security &amp; Token
              </h5>
            </div>
            <div className="card-body">
              <div className="mb-3">
                <label className="form-label small fw-semibold text-muted">Unique Assistant Token</label>
                <div className="input-group">
                  <input
                    type="text"
                    readOnly
                    className="form-control font-monospace bg-light"
                    value={stats?.assistantToken || 'Loading...'}
                  />
                  <button
                    onClick={() => copyToClipboard(stats?.assistantToken, 'url')}
                    className="btn btn-outline-secondary"
                    type="button"
                  >
                    <i className="bi bi-clipboard"></i>
                  </button>
                </div>
                <div className="form-text extra-small text-muted mt-1">
                  Server-side validated. Allows public visitor questions without exposing credentials or internal doc paths.
                </div>
              </div>

              <div className="mb-4">
                <label className="form-label small fw-semibold text-muted">Public Assistant URL</label>
                <div className="input-group">
                  <input
                    type="text"
                    readOnly
                    className="form-control font-monospace bg-light"
                    value={assistantUrl}
                  />
                  <button
                    onClick={() => copyToClipboard(assistantUrl, 'url')}
                    className="btn btn-outline-primary"
                    type="button"
                  >
                    <i className={`bi ${copiedUrl ? 'bi-check2' : 'bi-clipboard'}`}></i>
                    {copiedUrl ? ' Copied!' : ' Copy'}
                  </button>
                </div>
              </div>

              <div className="d-flex gap-2">
                <a
                  href={assistantUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="btn btn-primary btn-sm d-flex align-items-center gap-1 shadow-sm"
                >
                  <i className="bi bi-box-arrow-up-right"></i> Launch Fullscreen Chat
                </a>
              </div>
            </div>
          </div>
        </div>

        {/* Embed Mechanisms Card */}
        <div className="col-lg-6">
          <div className="card h-100 border-0 shadow-sm rounded-3">
            <div className="card-header bg-white py-3">
              <h5 className="fw-bold mb-0">
                <i className="bi bi-code-slash text-success me-2"></i>
                Embeddable Assistant Code
              </h5>
            </div>
            <div className="card-body">
              {/* Option A: Iframe */}
              <div className="mb-3">
                <div className="d-flex justify-content-between align-items-center mb-1">
                  <label className="form-label small fw-semibold text-muted mb-0">
                    Option A: Responsive iFrame Embed
                  </label>
                  <button
                    onClick={() => copyToClipboard(iframeCode, 'iframe')}
                    className="btn btn-link btn-sm p-0 text-decoration-none"
                  >
                    <i className={`bi ${copiedIframe ? 'bi-check2 text-success' : 'bi-clipboard'}`}></i>
                    {copiedIframe ? ' Copied!' : ' Copy HTML'}
                  </button>
                </div>
                <textarea
                  readOnly
                  rows={4}
                  className="form-control font-monospace small bg-light"
                  value={iframeCode}
                />
              </div>

              {/* Option B: Floating Widget JS */}
              <div className="mb-3">
                <div className="d-flex justify-content-between align-items-center mb-1">
                  <label className="form-label small fw-semibold text-muted mb-0">
                    Option B: Floating Chat Bubble (Script Tag)
                  </label>
                  <button
                    onClick={() => copyToClipboard(jsWidgetCode, 'js')}
                    className="btn btn-link btn-sm p-0 text-decoration-none"
                  >
                    <i className={`bi ${copiedJs ? 'bi-check2 text-success' : 'bi-clipboard'}`}></i>
                    {copiedJs ? ' Copied!' : ' Copy Script'}
                  </button>
                </div>
                <textarea
                  readOnly
                  rows={3}
                  className="form-control font-monospace small bg-light"
                  value={jsWidgetCode}
                />
              </div>

              <div className="alert alert-info py-2 px-3 extra-small mb-0">
                <i className="bi bi-lightbulb-fill me-1"></i>
                You can test both embed options live by opening <code>embed-demo.html</code> in your browser!
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Live Embed Preview */}
      <div className="card border-0 shadow-sm rounded-3">
        <div className="card-header bg-white py-3 d-flex justify-content-between align-items-center">
          <h5 className="fw-bold mb-0">
            <i className="bi bi-window-sidebar text-primary me-2"></i>
            Live Assistant Sandbox Preview
          </h5>
          <span className="badge bg-success-subtle text-success">Active Token Loaded</span>
        </div>
        <div className="card-body p-0" style={{ height: '550px' }}>
          {stats?.assistantToken ? (
            <iframe
              src={assistantUrl}
              title="Assistant Live Preview"
              style={{ width: '100%', height: '100%', border: 'none' }}
            ></iframe>
          ) : (
            <div className="text-center py-5 text-muted">
              <div className="spinner-border text-primary" role="status"></div>
              <p className="mt-2 small">Loading preview...</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default AssistantConfigView;
