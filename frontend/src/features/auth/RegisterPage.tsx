import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { authService } from '../../services/authService';
import type { Role } from '../../types/auth';
import { UserPlus, Mail, Lock, User, BadgeCheck, Building2, AlertCircle, CheckCircle2 } from 'lucide-react';

export default function RegisterPage() {
  const [formData, setFormData] = useState({ fullName: '', email: '', password: '', confirmPassword: '', employeeId: '', role: 'SALES' as Role });

  const registerMutation = useMutation({
    mutationFn: () => authService.register({ fullName: formData.fullName, email: formData.email, password: formData.password, employeeId: formData.employeeId, role: formData.role }),
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (formData.password !== formData.confirmPassword) { alert('Mật khẩu xác nhận không khớp!'); return; }
    registerMutation.mutate();
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-header"><div className="auth-logo"><UserPlus size={32} /></div><h1>Đăng ký tài khoản</h1><p>Tạo tài khoản mới trên hệ thống OOAS</p></div>
        <form onSubmit={handleSubmit} className="auth-form">
          {registerMutation.isError && <div className="alert alert-error"><AlertCircle size={18} /><span>{(registerMutation.error as any)?.response?.data?.message || 'Đăng ký thất bại'}</span></div>}
          {registerMutation.isSuccess && <div className="alert alert-success"><CheckCircle2 size={18} /><span>Đăng ký thành công! Vui lòng chờ phê duyệt từ Ban quản trị.</span></div>}
          <div className="form-group"><label htmlFor="fullName">Họ và tên</label><div className="input-wrapper"><User size={18} /><input id="fullName" name="fullName" placeholder="Nhập họ và tên" value={formData.fullName} onChange={handleChange} required /></div></div>
          <div className="form-group"><label htmlFor="reg-email">Email công ty</label><div className="input-wrapper"><Mail size={18} /><input id="reg-email" name="email" type="email" placeholder="Nhập email" value={formData.email} onChange={handleChange} required /></div></div>
          <div className="form-row">
            <div className="form-group"><label htmlFor="reg-password">Mật khẩu</label><div className="input-wrapper"><Lock size={18} /><input id="reg-password" name="password" type="password" placeholder="Tối thiểu 8 ký tự" value={formData.password} onChange={handleChange} required minLength={8} /></div></div>
            <div className="form-group"><label htmlFor="confirmPassword">Xác nhận</label><div className="input-wrapper"><Lock size={18} /><input id="confirmPassword" name="confirmPassword" type="password" placeholder="Nhập lại" value={formData.confirmPassword} onChange={handleChange} required /></div></div>
          </div>
          <div className="form-row">
            <div className="form-group"><label htmlFor="employeeId">Mã nhân viên</label><div className="input-wrapper"><BadgeCheck size={18} /><input id="employeeId" name="employeeId" placeholder="VD: EMP-001" value={formData.employeeId} onChange={handleChange} required /></div></div>
            <div className="form-group"><label htmlFor="role">Bộ phận</label><div className="input-wrapper"><Building2 size={18} /><select id="role" name="role" value={formData.role} onChange={handleChange}><option value="SALES">Bán hàng</option><option value="OVERSEAS_ORDER">Đặt hàng QT</option><option value="WAREHOUSE">Quản lý Kho</option></select></div></div>
          </div>
          <button type="submit" className="btn btn-primary btn-full" disabled={registerMutation.isPending || registerMutation.isSuccess}>{registerMutation.isPending ? 'Đang xử lý...' : 'Gửi đăng ký'}</button>
        </form>
        <p className="auth-footer">Đã có tài khoản? <Link to="/login">Đăng nhập</Link></p>
      </div>
    </div>
  );
}
