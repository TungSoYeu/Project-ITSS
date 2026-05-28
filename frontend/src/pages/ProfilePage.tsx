import React, { useState, useEffect } from 'react';
import { useAuth } from '../hooks/useAuth';
import { authService } from '../services/authService';
import { UserCircle, Shield, Save } from 'lucide-react';

export default function ProfilePage() {
  const { user } = useAuth();
  const [profileData, setProfileData] = useState({ fullName: '', employeeId: '' });
  const [passwordData, setPasswordData] = useState({ oldPassword: '', newPassword: '', confirmPassword: '' });
  const [loadingProfile, setLoadingProfile] = useState(false);
  const [loadingPassword, setLoadingPassword] = useState(false);
  const [profileMsg, setProfileMsg] = useState({ type: '', text: '' });
  const [passwordMsg, setPasswordMsg] = useState({ type: '', text: '' });

  useEffect(() => {
    if (user) {
      setProfileData({ fullName: user.fullName || '', employeeId: user.employeeId || '' });
    }
  }, [user]);

  const handleProfileSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoadingProfile(true);
    setProfileMsg({ type: '', text: '' });
    try {
      const res = await authService.updateProfile(profileData);
      setProfileMsg({ type: 'success', text: res.message || 'Cập nhật thông tin thành công' });
      // In a real app we might update the AuthContext here so the sidebar reflects changes instantly
      // For now, refreshing the page or relogging will do, or the context will refresh if implemented
    } catch (error: any) {
      setProfileMsg({ type: 'error', text: error.response?.data?.error || 'Có lỗi xảy ra' });
    } finally {
      setLoadingProfile(false);
    }
  };

  const handlePasswordSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoadingPassword(true);
    setPasswordMsg({ type: '', text: '' });
    
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      setPasswordMsg({ type: 'error', text: 'Mật khẩu xác nhận không khớp' });
      setLoadingPassword(false);
      return;
    }
    if (passwordData.newPassword.length < 8) {
      setPasswordMsg({ type: 'error', text: 'Mật khẩu mới phải có ít nhất 8 ký tự' });
      setLoadingPassword(false);
      return;
    }

    try {
      const res = await authService.changePassword({ oldPassword: passwordData.oldPassword, newPassword: passwordData.newPassword });
      setPasswordMsg({ type: 'success', text: res.message || 'Đổi mật khẩu thành công' });
      setPasswordData({ oldPassword: '', newPassword: '', confirmPassword: '' });
    } catch (error: any) {
      setPasswordMsg({ type: 'error', text: error.response?.data?.error || 'Có lỗi xảy ra' });
    } finally {
      setLoadingPassword(false);
    }
  };

  return (
    <div className="dashboard-container">
      <div className="header-actions">
        <h1>Hồ sơ cá nhân</h1>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem', marginTop: '2rem' }}>
        
        {/* Card Thông tin */}
        <div className="stat-card" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div className="flex-row" style={{ alignItems: 'center', gap: '0.5rem', color: 'var(--primary)', marginBottom: '1rem' }}>
            <UserCircle size={28} />
            <h2 style={{ fontSize: '1.2rem', margin: 0 }}>Thông tin cơ bản</h2>
          </div>
          
          <form onSubmit={handleProfileSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div className="form-group">
              <label>Họ và tên</label>
              <div className="input-wrapper">
                <input 
                  type="text" 
                  value={profileData.fullName} 
                  onChange={e => setProfileData({...profileData, fullName: e.target.value})} 
                  required
                />
              </div>
            </div>
            <div className="form-group">
              <label>Mã nhân viên</label>
              <div className="input-wrapper">
                <input 
                  type="text" 
                  value={profileData.employeeId} 
                  onChange={e => setProfileData({...profileData, employeeId: e.target.value})} 
                  required
                />
              </div>
            </div>
            <div className="form-group">
              <label>Email (Tài khoản)</label>
              <div className="input-wrapper">
                <input type="text" value={user?.email || ''} disabled style={{ background: 'var(--bg-card)', color: 'var(--text-muted)' }} />
              </div>
            </div>
            <div className="form-group">
              <label>Vai trò</label>
              <div className="input-wrapper">
                <input type="text" value={user?.role || ''} disabled style={{ background: 'var(--bg-card)', color: 'var(--text-muted)' }} />
              </div>
            </div>

            {profileMsg.text && (
              <div className={`alert ${profileMsg.type === 'error' ? 'alert-error' : 'alert-success'}`}>
                {profileMsg.text}
              </div>
            )}

            <button type="submit" className="btn btn-primary" style={{ marginTop: '1rem', alignSelf: 'flex-start' }} disabled={loadingProfile}>
              <Save size={18} /> {loadingProfile ? 'Đang lưu...' : 'Lưu thông tin'}
            </button>
          </form>
        </div>

        {/* Card Đổi mật khẩu */}
        <div className="stat-card" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div className="flex-row" style={{ alignItems: 'center', gap: '0.5rem', color: 'var(--warning)', marginBottom: '1rem' }}>
            <Shield size={28} />
            <h2 style={{ fontSize: '1.2rem', margin: 0 }}>Đổi mật khẩu</h2>
          </div>

          <form onSubmit={handlePasswordSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div className="form-group">
              <label>Mật khẩu hiện tại</label>
              <div className="input-wrapper">
                <input 
                  type="password" 
                  value={passwordData.oldPassword} 
                  onChange={e => setPasswordData({...passwordData, oldPassword: e.target.value})} 
                  required
                />
              </div>
            </div>
            <div className="form-group">
              <label>Mật khẩu mới (Tối thiểu 8 ký tự)</label>
              <div className="input-wrapper">
                <input 
                  type="password" 
                  value={passwordData.newPassword} 
                  onChange={e => setPasswordData({...passwordData, newPassword: e.target.value})} 
                  required
                  minLength={8}
                />
              </div>
            </div>
            <div className="form-group">
              <label>Xác nhận mật khẩu mới</label>
              <div className="input-wrapper">
                <input 
                  type="password" 
                  value={passwordData.confirmPassword} 
                  onChange={e => setPasswordData({...passwordData, confirmPassword: e.target.value})} 
                  required
                  minLength={8}
                />
              </div>
            </div>

            {passwordMsg.text && (
              <div className={`alert ${passwordMsg.type === 'error' ? 'alert-error' : 'alert-success'}`}>
                {passwordMsg.text}
              </div>
            )}

            <button type="submit" className="btn btn-outline" style={{ marginTop: '1rem', alignSelf: 'flex-start' }} disabled={loadingPassword}>
              <Shield size={18} /> {loadingPassword ? 'Đang đổi...' : 'Đổi mật khẩu'}
            </button>
          </form>
        </div>

      </div>
    </div>
  );
}
