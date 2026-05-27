import { useAuth } from '../hooks/useAuth';
import { useNavigate } from 'react-router-dom';
import { LayoutDashboard, ClipboardList, Truck, Package, MapPin, Warehouse, LogOut } from 'lucide-react';

export default function DashboardPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => { logout(); navigate('/login'); };
  const menuItems = getMenuByRole(user?.role);

  return (
    <div className="dashboard-layout">
      <aside className="sidebar">
        <div className="sidebar-header"><LayoutDashboard size={24} /><span>OOAS</span></div>
        <nav className="sidebar-nav">
          {menuItems.map((item) => (
            <button key={item.label} className="sidebar-link" onClick={() => navigate(item.path)}>{item.icon}<span>{item.label}</span></button>
          ))}
        </nav>
        <div className="sidebar-footer">
          <div className="user-info"><span className="user-name">{user?.fullName}</span><span className="user-role">{getRoleLabel(user?.role)}</span></div>
          <button className="btn-icon" onClick={handleLogout} title="Đăng xuất"><LogOut size={20} /></button>
        </div>
      </aside>
      <main className="main-content">
        <div className="dashboard-header"><h1>Trang chủ</h1><p>Xin chào, <strong>{user?.fullName}</strong>. Chào mừng bạn đến với hệ thống OOAS.</p></div>
        <div className="stats-grid">
          <div className="stat-card"><ClipboardList size={28} /><div><span className="stat-label">Yêu cầu chờ xử lý</span><span className="stat-value">—</span></div></div>
          <div className="stat-card"><Truck size={28} /><div><span className="stat-label">Đơn đang vận chuyển</span><span className="stat-value">—</span></div></div>
          <div className="stat-card"><Package size={28} /><div><span className="stat-label">Đơn cần nhập kho</span><span className="stat-value">—</span></div></div>
        </div>
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
    OVERSEAS_ORDER: [{ label: 'Yêu cầu nhập hàng', path: '/order-requests', icon: <ClipboardList size={20} /> }, { label: 'Đơn đặt hàng', path: '/purchase-orders', icon: <Package size={20} /> }],
    WAREHOUSE: [{ label: 'Nhập kho', path: '/warehouse', icon: <Warehouse size={20} /> }],
    ADMIN: [{ label: 'Yêu cầu nhập hàng', path: '/order-requests', icon: <ClipboardList size={20} /> }, { label: 'Đơn đặt hàng', path: '/purchase-orders', icon: <Package size={20} /> }, { label: 'Quản lý Site', path: '/sites', icon: <MapPin size={20} /> }, { label: 'Theo dõi VC', path: '/shipments', icon: <Truck size={20} /> }, { label: 'Nhập kho', path: '/warehouse', icon: <Warehouse size={20} /> }],
  };
  return [...common, ...(roleMenus[role || ''] || [])];
}
