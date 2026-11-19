import React, { useState, useEffect } from 'react';
import { documentApi } from '../api/documentApi';

const DocumentsView = ({ user }) => {
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [selectedFile, setSelectedFile] = useState(null);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  const fetchDocuments = async () => {
    setLoading(true);
    try {
      const data = await documentApi.getDocuments();
      setDocuments(data);
    } catch (err) {
      setError('Failed to fetch documents: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDocuments();
  }, []);

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    setError(null);

    if (file) {
      // Client-side validations
      if (!file.name.toLowerCase().endsWith('.pdf')) {
        setError('Only PDF documents (.pdf) are allowed.');
        setSelectedFile(null);
        return;
      }
      if (file.size > 10 * 1024 * 1024) {
        setError('File size exceeds the maximum limit of 10 MB per PDF.');
        setSelectedFile(null);
        return;
      }
      setSelectedFile(file);
    }
  };

  const handleUpload = async (e) => {
    e.preventDefault();
    if (!selectedFile) return;

    if (documents.length >= 10) {
      setError('Organization has reached the maximum limit of 10 PDF documents. Please delete an existing document first.');
      return;
    }

    setUploading(true);
    setError(null);
    setSuccess(null);

    try {
      const res = await documentApi.uploadDocument(selectedFile);
      setSuccess(`Document '${res.originalFileName}' (${res.pageCount} pages, ${res.chunkCount} chunks) successfully processed and indexed!`);
      setSelectedFile(null);
      // Reset input element
      const fileInput = document.getElementById('pdfFileInput');
      if (fileInput) fileInput.value = '';
      fetchDocuments();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to upload document. ' + err.message);
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = async (docId, fileName) => {
    if (!window.confirm(`Are you sure you want to delete '${fileName}'? All extracted chunks will be removed from retrieval.`)) {
      return;
    }

    try {
      await documentApi.deleteDocument(docId);
      setSuccess(`Document '${fileName}' was successfully deleted.`);
      fetchDocuments();
    } catch (err) {
      setError('Failed to delete document: ' + (err.response?.data?.message || err.message));
    }
  };

  const formatBytes = (bytes) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  return (
    <div className="container py-4">
      {/* Header */}
      <div className="d-flex flex-wrap justify-content-between align-items-center mb-4 pb-3 border-bottom gap-3">
        <div>
          <h2 className="fw-bold mb-1">
            <i className="bi bi-folder-fill text-primary me-2"></i>
            Knowledge Base Management
          </h2>
          <p className="text-muted small mb-0">
            Organization: <strong>{user?.organizationName}</strong> | Upload internal PDFs to ground your AI assistant
          </p>
        </div>

        <div>
          <span className={`badge ${documents.length >= 10 ? 'bg-danger' : 'bg-primary'} fs-6 p-2`}>
            {documents.length} / 10 Documents
          </span>
        </div>
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

      {/* Upload Card */}
      <div className="card border-0 shadow-sm rounded-3 mb-4">
        <div className="card-header bg-white py-3">
          <h5 className="fw-bold mb-0">
            <i className="bi bi-cloud-arrow-up-fill text-primary me-2"></i>
            Upload New PDF Document
          </h5>
        </div>
        <div className="card-body">
          <form onSubmit={handleUpload}>
            <div className="row g-3 align-items-center">
              <div className="col-md-8">
                <input
                  id="pdfFileInput"
                  type="file"
                  accept="application/pdf"
                  className="form-control"
                  onChange={handleFileChange}
                  disabled={uploading || documents.length >= 10}
                />
                <div className="form-text small text-muted mt-1">
                  <i className="bi bi-info-circle me-1"></i>
                  Mandatory Limits: Max <strong>10 MB</strong> size, Max <strong>20 pages</strong> per PDF, Max <strong>10 documents</strong> total.
                </div>
              </div>
              <div className="col-md-4">
                <button
                  type="submit"
                  disabled={!selectedFile || uploading || documents.length >= 10}
                  className="btn btn-primary w-100 py-2 fw-semibold d-flex align-items-center justify-content-center gap-2 shadow-sm"
                >
                  {uploading ? (
                    <>
                      <span className="spinner-border spinner-border-sm"></span>
                      <span>Extracting &amp; Indexing...</span>
                    </>
                  ) : (
                    <>
                      <i className="bi bi-upload"></i>
                      <span>Upload &amp; Index PDF</span>
                    </>
                  )}
                </button>
              </div>
            </div>
          </form>
        </div>
      </div>

      {/* Documents Table */}
      <div className="card border-0 shadow-sm rounded-3">
        <div className="card-header bg-white py-3 d-flex justify-content-between align-items-center">
          <h5 className="fw-bold mb-0">
            <i className="bi bi-file-earmark-text text-secondary me-2"></i>
            Indexed Documents ({documents.length})
          </h5>
          <button onClick={fetchDocuments} className="btn btn-sm btn-outline-secondary">
            <i className="bi bi-arrow-clockwise"></i> Refresh
          </button>
        </div>

        <div className="card-body p-0">
          {loading ? (
            <div className="text-center py-5">
              <div className="spinner-border text-primary" role="status"></div>
              <p className="mt-2 text-muted small">Loading knowledge base documents...</p>
            </div>
          ) : documents.length === 0 ? (
            <div className="text-center py-5 text-muted">
              <i className="bi bi-file-earmark-plus fs-1 d-block mb-2 text-secondary opacity-50"></i>
              <p className="mb-1">No documents uploaded yet.</p>
              <p className="small">Upload sample PDFs from <code>sample-data/</code> to test knowledge retrieval!</p>
            </div>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light small text-secondary">
                  <tr>
                    <th>Document Name</th>
                    <th>Pages</th>
                    <th>Chunks</th>
                    <th>File Size</th>
                    <th>Upload Date</th>
                    <th className="text-end">Actions</th>
                  </tr>
                </thead>
                <tbody className="small">
                  {documents.map((doc) => (
                    <tr key={doc.id}>
                      <td>
                        <div className="d-flex align-items-center gap-2">
                          <i className="bi bi-file-earmark-pdf-fill text-danger fs-5"></i>
                          <div>
                            <div className="fw-semibold text-dark">{doc.originalFileName}</div>
                            <div className="text-muted extra-small">ID: {doc.id}</div>
                          </div>
                        </div>
                      </td>
                      <td>
                        <span className="badge bg-light text-dark border">
                          {doc.pageCount} {doc.pageCount === 1 ? 'Page' : 'Pages'}
                        </span>
                      </td>
                      <td>
                        <span className="badge bg-info-subtle text-info-emphasis border">
                          {doc.chunkCount} Chunks
                        </span>
                      </td>
                      <td className="text-muted">
                        {formatBytes(doc.fileSize)}
                      </td>
                      <td className="text-muted">
                        {new Date(doc.uploadedAt).toLocaleDateString()} {new Date(doc.uploadedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </td>
                      <td className="text-end">
                        <button
                          onClick={() => handleDelete(doc.id, doc.originalFileName)}
                          className="btn btn-outline-danger btn-sm"
                          title="Delete document and chunks"
                        >
                          <i className="bi bi-trash"></i> Delete
                        </button>
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

export default DocumentsView;
