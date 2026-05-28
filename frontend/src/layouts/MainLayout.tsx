import { useAuth } from '../hooks/useAuth';
import { useNavigate, useLocation } from 'react-router-dom';
import { LayoutDashboard, ClipboardList, Truck, Package, MapPin, Warehouse, LogOut } from 'lucide-react';
import React from 'react';

export default function MainLayout({ children }: { children: React.ReactNode }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => { logout(); navigate('/login'); };
  const menuItems = getMenuByRole(user?.role);

  return (
    <div className="dashboard-layout">
      <aside className="sidebar">
        <div className="sidebar-header"><LayoutDashboard size={24} /><span>OOAS</span></div>
        <nav className="sidebar-nav">
          {menuItems.map((item) => {
            const isActive = location.pathname.startsWith(item.path);
            return (
              <button key={item.label} className={`sidebar-link ${isActive ? 'active' : ''}`} onClick={() => navigate(item.path)} style={isActive ? { background: 'rgba(255,255,255,0.1)', color: 'white' } : {}}>
                {item.icon}<span>{item.label}</span>
              </button>
            );
          })}
        </nav>
        <div className="sidebar-footer">
          <div 
            className="user-info" 
            onClick={() => navigate('/profile')}
            style={{ cursor: 'pointer', padding: '0.5rem', borderRadius: '8px', transition: 'background 0.2s', display: 'flex', alignItems: 'center', gap: '0.75rem' }}
            onMouseEnter={e => e.currentTarget.style.background = 'rgba(255,255,255,0.05)'}
            onMouseLeave={e => e.currentTarget.style.background = 'transparent'}
            title="Xem hồ sơ cá nhân"
          >
            <div style={{ width: '36px', height: '36px', borderRadius: '50%', background: 'var(--primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white', fontWeight: 'bold', fontSize: '1.2rem' }}>
              {user?.fullName?.charAt(0).toUpperCase() || 'U'}
            </div>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <span className="user-name" style={{ fontSize: '0.9rem', fontWeight: 600 }}>{user?.fullName}</span>
              <span className="user-role" style={{ fontSize: '0.75rem', opacity: 0.7 }}>{getRoleLabel(user?.role)}</span>
            </div>
          </div>
          <button className="btn-icon" onClick={handleLogout} title="Đăng xuất"><LogOut size={20} /></button>
        </div>
      </aside>
      <main className="main-content">
        {children}
      </main>
    </div>
  );
}

function getRoleLabel(role?: string) {
  const map: Record<string, string> = { ADMIN: 'Quản trị viên', SALES: 'Bộ phận Bán hàng', OVERSEAS_ORDER: 'Đặt hàng Quốc tế', WAREHOUSE: 'Quản lý Kho', SUPPLIER: 'Nhà cung cấp' };
  return map[role || ''] || role;
}

function getMenuByRole(role?: string) {
  const common = [{ label: 'Trang chủ', path: '/dashboard', icon: <LayoutDashboard size={20} /> }];
  const roleMenus: Record<string, typeof common> = {
    SALES: [{ label: 'Yêu cầu nhập hàng', path: '/order-requests', icon: <ClipboardList size={20} /> }],
    OVERSEAS_ORDER: [{ label: 'Yêu cầu nhập hàng', path: '/order-requests', icon: <ClipboardList size={20} /> }, { label: 'Đơn đặt hàng', path: '/purchase-orders', icon: <Package size={20} /> }, { label: 'Quản lý Site', path: '/sites', icon: <MapPin size={20} /> }, { label: 'Theo dõi VC', path: '/shipments', icon: <Truck size={20} /> }],
    WAREHOUSE: [{ label: 'Theo dõi VC', path: '/shipments', icon: <Truck size={20} /> }, { label: 'Nhập kho', path: '/warehouse', icon: <Warehouse size={20} /> }],
    SUPPLIER: [{ label: 'Đơn đặt hàng', path: '/purchase-orders', icon: <Package size={20} /> }, { label: 'Theo dõi VC', path: '/shipments', icon: <Truck size={20} /> }],
    ADMIN: [{ label: 'Yêu cầu nhập hàng', path: '/order-requests', icon: <ClipboardList size={20} /> }, { label: 'Đơn đặt hàng', path: '/purchase-orders', icon: <Package size={20} /> }, { label: 'Quản lý Site', path: '/sites', icon: <MapPin size={20} /> }, { label: 'Theo dõi VC', path: '/shipments', icon: <Truck size={20} /> }, { label: 'Nhập kho', path: '/warehouse', icon: <Warehouse size={20} /> }],
  };
  return [...common, ...(roleMenus[role || ''] || [])];
}
