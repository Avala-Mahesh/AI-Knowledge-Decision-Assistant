import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '../api/authApi';

const Login = ({ onLoginSuccess }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const data = await authApi.login({ email, password });
      localStorage.setItem('token', data.token);
      localStorage.setItem('user', JSON.stringify(data));
      if (onLoginSuccess) onLoginSuccess(data);
      navigate('/dashboard');
    } catch (err) {
      setError(
        err.response?.data?.message ||
        'Failed to log in. Please check your credentials.'
      );
    } finally {
      setLoading(false);
    }
  };

  // const handlePreFill = (orgName, prefillEmail, prefillPass) => {
  //   setEmail(prefillEmail);
  //   setPassword(prefillPass);
  // };

  return (
    <div className="container py-5">
      <div className="row justify-content-center">
        <div className="col-md-6 col-lg-5">
          <div className="card shadow-lg border-0 rounded-4">
            <div className="card-body p-4 p-md-5">
              <div className="text-center mb-4">
                <div className="d-inline-flex p-3 bg-primary bg-opacity-10 rounded-circle text-primary mb-2">
                  <i className="bi bi-shield-lock-fill fs-2"></i>
                </div>
                <h3 className="fw-bold">Sign In to Dashboard</h3>
                <p className="text-muted small">
                  Manage your organization's AI Knowledge Base & Assistant
                </p>
              </div>

              {error && (
                <div className="alert alert-danger py-2 small d-flex align-items-center gap-2" role="alert">
                  <i className="bi bi-exclamation-triangle-fill"></i>
                  <span>{error}</span>
                </div>
              )}

              <form onSubmit={handleLogin}>
                <div className="mb-3">
                  <label className="form-label small fw-semibold">Email Address</label>
                  <div className="input-group">
                    <span className="input-group-text bg-light"><i className="bi bi-envelope"></i></span>
                    <input
                      type="email"
                      className="form-control"
                      placeholder="mahesh@avala.com"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
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
                      className="form-control"
                      placeholder="••••••••"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      required
                    />
                  </div>
                </div>

                <button
                  type="submit"
                  disabled={loading}
                  className="btn btn-primary w-100 py-2 fw-semibold shadow-sm mb-3"
                >
                  {loading ? (
                    <span><span className="spinner-border spinner-border-sm me-2"></span>Signing In...</span>
                  ) : (
                    <span>Sign In <i className="bi bi-arrow-right ms-1"></i></span>
                  )}
                </button>
              </form>

              <div className="text-center small text-muted">
                Don't have an organization account yet?{' '}
                <Link to="/register" className="text-primary fw-semibold text-decoration-none">
                  Register here
                </Link>
              </div>

              {/* <div className="mt-4 pt-3 border-top text-center">
                <div className="small text-muted mb-2 fw-semibold">Demo Preset Shortcuts:</div>
                <div className="d-flex justify-content-center gap-2">
                  <button
                    type="button"
                    onClick={() => handlePreFill('Apex University', 'admin@apex.edu', 'password123')}
                    className="btn btn-sm btn-outline-secondary"
                  >
                    Apex University
                  </button>
                  <button
                    type="button"
                    onClick={() => handlePreFill('Nexus Corp', 'admin@nexuscorp.com', 'password123')}
                    className="btn btn-sm btn-outline-secondary"
                  >
                    Nexus Corp
                  </button>
                </div>
              </div> */}

            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
