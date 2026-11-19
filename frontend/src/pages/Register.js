import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '../api/authApi';

const Register = ({ onLoginSuccess }) => {
  const [formData, setFormData] = useState({
    organizationName: '',
    fullName: '',
    email: '',
    password: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const data = await authApi.register(formData);
      localStorage.setItem('token', data.token);
      localStorage.setItem('user', JSON.stringify(data));
      if (onLoginSuccess) onLoginSuccess(data);
      navigate('/dashboard');
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.response?.data?.validationErrors?.organizationName ||
        err.response?.data?.validationErrors?.email ||
        'Failed to create organization account.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container py-5">
      <div className="row justify-content-center">
        <div className="col-md-7 col-lg-6">
          <div className="card shadow-lg border-0 rounded-4">
            <div className="card-body p-4 p-md-5">
              <div className="text-center mb-4">
                <div className="d-inline-flex p-3 bg-success bg-opacity-10 rounded-circle text-success mb-2">
                  <i className="bi bi-building-add fs-2"></i>
                </div>
                <h3 className="fw-bold">Create Organization Account</h3>
                <p className="text-muted small">
                  Create a knowledge base and AI decision assistant
                </p>
              </div>

              {error && (
                <div className="alert alert-danger py-2 small d-flex align-items-center gap-2" role="alert">
                  <i className="bi bi-exclamation-triangle-fill"></i>
                  <span>{error}</span>
                </div>
              )}

              <form onSubmit={handleRegister}>
                <div className="mb-3">
                  <label className="form-label small fw-semibold">Organization Name</label>
                  <div className="input-group">
                    <span className="input-group-text bg-light"><i className="bi bi-building"></i></span>
                    <input
                      type="text"
                      name="organizationName"
                      className="form-control"
                      placeholder="e.g. Apex University or Nexus Corp"
                      value={formData.organizationName}
                      onChange={handleChange}
                      required
                    />
                  </div>
                </div>

                <div className="mb-3">
                  <label className="form-label small fw-semibold">Full Name</label>
                  <div className="input-group">
                    <span className="input-group-text bg-light"><i className="bi bi-person"></i></span>
                    <input
                      type="text"
                      name="fullName"
                      className="form-control"
                      placeholder="e.g. Jane Doe"
                      value={formData.fullName}
                      onChange={handleChange}
                      required
                    />
                  </div>
                </div>

                <div className="mb-3">
                  <label className="form-label small fw-semibold">Email</label>
                  <div className="input-group">
                    <span className="input-group-text bg-light"><i className="bi bi-envelope"></i></span>
                    <input
                      type="email"
                      name="email"
                      className="form-control"
                      placeholder="admin@organization.com"
                      value={formData.email}
                      onChange={handleChange}
                      required
                    />
                  </div>
                </div>

                <div className="mb-4">
                  <label className="form-label small fw-semibold">Password</label>
                  <div className="input-group">
                    <span className="input-group-text bg-light"><i className="bi bi-key"></i></span>
                    <input
                      type="password"
                      name="password"
                      className="form-control"
                      placeholder="At least 6 characters"
                      value={formData.password}
                      onChange={handleChange}
                      minLength={6}
                      required
                    />
                  </div>
                </div>

                <button
                  type="submit"
                  disabled={loading}
                  className="btn btn-success w-100 py-2 fw-semibold shadow-sm mb-3"
                >
                  {loading ? (
                    <span><span className="spinner-border spinner-border-sm me-2"></span>Creating Account...</span>
                  ) : (
                    <span>Create Account &amp; Assistant <i className="bi bi-check-circle ms-1"></i></span>
                  )}
                </button>
              </form>

              <div className="text-center small text-muted">
                Already have an account?{' '}
                <Link to="/login" className="text-primary fw-semibold text-decoration-none">
                  Sign in
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;
