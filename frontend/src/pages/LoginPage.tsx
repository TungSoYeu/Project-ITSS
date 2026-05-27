import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { authService } from '../services/authService';
import { useAuth } from '../hooks/useAuth';
import { LogIn, Mail, Lock, AlertCircle } from 'lucide-react';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();
  const { login } = useAuth();

  const loginMutation = useMutation({
    mutationFn: () => authService.login({ email, password }),
    onSuccess: (data: any) => {
      login(data.access_token, data.user);
      navigate('/dashboard');
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    loginMutation.mutate();
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-header">
          <div className="auth-logo"><LogIn size={32} /></div>
          <h1>Đăng nhập OOAS</h1>
          <p>Hệ thống Tự động hóa Đặt hàng Quốc tế</p>
        </div>
        <form onSubmit={handleSubmit} className="auth-form">
          {loginMutation.isError && (
            <div className="alert alert-error">
              <AlertCircle size={18} />
              <span>{(loginMutation.error as any)?.response?.data?.message || 'Đăng nhập thất bại'}</span>
            </div>
          )}
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <div className="input-wrapper"><Mail size={18} /><input id="email" type="email" placeholder="Nhập email công ty" value={email} onChange={(e) => setEmail(e.target.value)} required /></div>
          </div>
          <div className="form-group">
            <label htmlFor="password">Mật khẩu</label>
            <div className="input-wrapper"><Lock size={18} /><input id="password" type="password" placeholder="Nhập mật khẩu" value={password} onChange={(e) => setPassword(e.target.value)} required /></div>
          </div>
          <button type="submit" className="btn btn-primary btn-full" disabled={loginMutation.isPending}>
            {loginMutation.isPending ? 'Đang xử lý...' : 'Đăng nhập'}
          </button>
        </form>
        <p className="auth-footer">Chưa có tài khoản? <Link to="/register">Đăng ký tại đây</Link></p>
      </div>
    </div>
  );
}
