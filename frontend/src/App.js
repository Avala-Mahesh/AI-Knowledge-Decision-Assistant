import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import DocumentsView from './pages/DocumentsView';
import AssistantConfigView from './pages/AssistantConfigView';
import VisitorChatView from './pages/VisitorChatView';
import { authApi } from './api/authApi';

function App() {
  const [currentUser, setCurrentUser] = useState(authApi.getCurrentUser());

  useEffect(() => {
    // If token exists, refresh user profile
    if (authApi.isAuthenticated()) {
      authApi.getMe()
        .then((data) => {
          const userObj = {
            userId: data.userId,
            email: data.email,
            fullName: data.fullName,
            organizationId: data.organizationId,
            organizationName: data.organizationName,
            assistantToken: data.assistantToken,
          };
          localStorage.setItem('user', JSON.stringify(userObj));
          setCurrentUser(userObj);
        })
        .catch(() => {
          authApi.logout();
          setCurrentUser(null);
        });
    }
  }, []);

  const handleLoginSuccess = (userObj) => {
    setCurrentUser(userObj);
  };

  const handleLogout = () => {
    setCurrentUser(null);
  };

  const ProtectedRoute = ({ children }) => {
    if (!authApi.isAuthenticated()) {
      return <Navigate to="/login" replace />;
    }
    return (
      <div className="d-flex flex-column min-vh-100 bg-light">
        <Navbar user={currentUser} onLogout={handleLogout} />
        <main className="flex-grow-1">{children}</main>
        <footer className="bg-white border-top py-3 text-center text-muted small">
          AI Knowledge &amp; Decision Assistant &copy; 2024 &bull; Multi-Tenant SaaS Engine
        </footer>
      </div>
    );
  };

  return (
    <Router>
      <Routes>
        {/* Public Visitor Assistant URL (no login needed) */}
        <Route path="/chat/:token" element={<VisitorChatView />} />

        {/* Public Auth Routes */}
        <Route
          path="/login"
          element={
            authApi.isAuthenticated() ? (
              <Navigate to="/dashboard" replace />
            ) : (
              <Login onLoginSuccess={handleLoginSuccess} />
            )
          }
        />
        <Route
          path="/register"
          element={
            authApi.isAuthenticated() ? (
              <Navigate to="/dashboard" replace />
            ) : (
              <Register onLoginSuccess={handleLoginSuccess} />
            )
          }
        />

        {/* Authenticated Admin Dashboard Routes */}
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard user={currentUser} />
            </ProtectedRoute>
          }
        />
        <Route
          path="/documents"
          element={
            <ProtectedRoute>
              <DocumentsView user={currentUser} />
            </ProtectedRoute>
          }
        />
        <Route
          path="/assistant-config"
          element={
            <ProtectedRoute>
              <AssistantConfigView user={currentUser} />
            </ProtectedRoute>
          }
        />

        {/* Fallback */}
        <Route
          path="*"
          element={<Navigate to={authApi.isAuthenticated() ? '/dashboard' : '/login'} replace />}
        />
      </Routes>
    </Router>
  );
}

export default App;
