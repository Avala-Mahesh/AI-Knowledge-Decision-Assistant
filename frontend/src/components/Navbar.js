import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '../api/authApi';

const Navbar = ({ user, onLogout }) => {
  const navigate = useNavigate();

  const handleLogout = () => {
    authApi.logout();
    if (onLogout) onLogout();
    navigate('/login');
  };

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark px-3 py-2 border-bottom border-secondary shadow-sm">
      <div className="container-fluid">
        <Link className="navbar-brand d-flex align-items-center gap-2 fw-bold" to="/dashboard">
          <span className="badge bg-primary p-2 fs-6">
            <i className="bi bi-robot"></i>
          </span>
          <span>AI Decision Assistant</span>
          <span className="badge bg-secondary ms-1 fs-xs fw-normal">SaaS MVP</span>
        </Link>

        <button
          className="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navbarContent"
        >
          <span className="navbar-toggler-icon"></span>
        </button>

        <div className="collapse navbar-collapse" id="navbarContent">
          <ul className="navbar-nav me-auto mb-2 mb-lg-0">
            <li className="nav-item">
              <Link className="nav-link" to="/dashboard">
                <i className="bi bi-speedometer2 me-1"></i> Dashboard
              </Link>
            </li>
            <li className="nav-item">
              <Link className="nav-link" to="/documents">
                <i className="bi bi-file-earmark-pdf me-1"></i> Knowledge Base
              </Link>
            </li>
            <li className="nav-item">
              <Link className="nav-link" to="/assistant-config">
                <i className="bi bi-gear-wide-connected me-1"></i> Assistant & Embed
              </Link>
            </li>
            <li className="nav-item">
              <a
                className="nav-link"
                href="http://localhost:8080/swagger-ui/index.html"
                target="_blank"
                rel="noreferrer"
              >
                <i className="bi bi-code-slash me-1"></i> API Docs (Swagger)
              </a>
            </li>
          </ul>

          <div className="d-flex align-items-center gap-3">
            {user && (
              <div className="text-light text-end d-none d-md-block">
                <div className="fw-semibold small">
                  <i className="bi bi-building me-1 text-info"></i>
                  {user.organizationName}
                </div>
                <div className="text-muted" style={{ fontSize: '0.75rem' }}>
                  {user.email}
                </div>
              </div>
            )}
            <button
              onClick={handleLogout}
              className="btn btn-outline-danger btn-sm d-flex align-items-center gap-1"
            >
              <i className="bi bi-box-arrow-right"></i> Logout
            </button>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
