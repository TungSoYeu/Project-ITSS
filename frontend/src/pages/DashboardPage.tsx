import { useAuth } from '../hooks/useAuth';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { dashboardService } from '../services/dashboardService';
import { ClipboardList, Gauge, Package, Ship, Truck } from 'lucide-react';
import { purchaseOrderService } from '../services/purchaseOrderService';
import { catalogService } from '../services/catalogService';

export default function DashboardPage() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const { data: stats, isLoading } = useQuery({
    queryKey: ['dashboard-summary'],
    queryFn: dashboardService.getSummary,
  });

  const { data: purchaseOrders } = useQuery({
    queryKey: ['purchase-orders', 'dashboard'],
    queryFn: () => purchaseOrderService.list(),
  });

  const { data: sites } = useQuery({
    queryKey: ['sites', 'dashboard'],
    queryFn: () => catalogService.listSites(true),
  });

  const seaOrders = purchaseOrders?.filter((order) => order.transportMethod === 'SEA').length || 0;
  const airOrders = purchaseOrders?.filter((order) => order.transportMethod === 'AIR').length || 0;
  const seaRatio = purchaseOrders?.length ? Math.round((seaOrders / purchaseOrders.length) * 100) : 0;
  const airRatio = purchaseOrders?.length ? Math.round((airOrders / purchaseOrders.length) * 100) : 0;
  const currentMonth = new Date().toISOString().slice(0, 7);
  const monthlyPoCount = purchaseOrders?.filter((order) => order.createdAt.slice(0, 7) === currentMonth).length || 0;
  const maxLeadTime = Math.max(1, ...(sites?.map((site) => Math.max(site.seaLeadTime, site.airLeadTime)) || [1]));

  return (
    <>
      <div className="dashboard-header">
        <h1>Trang chủ</h1>
        <p>Xin chào, <strong>{user?.fullName}</strong>. Chào mừng bạn đến với hệ thống OOAS.</p>
      </div>
      <div className="stats-grid">
        <div className="stat-card" style={{ cursor: 'pointer' }} onClick={() => navigate('/order-requests')}><ClipboardList size={28} /><div><span className="stat-label">Yêu cầu chờ xử lý</span><span className="stat-value">{isLoading ? '...' : (stats?.pendingRequests || 0)}</span></div></div>
        <div className="stat-card" style={{ cursor: 'pointer' }} onClick={() => navigate('/order-requests')}><Gauge size={28} /><div><span className="stat-label">Yêu cầu đang tối ưu</span><span className="stat-value">{isLoading ? '...' : (stats?.processingRequests || 0)}</span></div></div>
        <div className="stat-card" style={{ cursor: 'pointer' }} onClick={() => navigate('/shipments')}><Truck size={28} /><div><span className="stat-label">Đơn đang vận chuyển</span><span className="stat-value">{isLoading ? '...' : (stats?.shippingOrders || 0)}</span></div></div>
        <div className="stat-card" style={{ cursor: 'pointer' }} onClick={() => navigate('/warehouse')}><Package size={28} /><div><span className="stat-label">Đơn cần nhập kho</span><span className="stat-value">{isLoading ? '...' : (stats?.warehouseInboundOrders || 0)}</span></div></div>
        <div className="stat-card" style={{ cursor: 'pointer' }} onClick={() => navigate('/purchase-orders')}><Ship size={28} /><div><span className="stat-label">Tỷ lệ Sea</span><span className="stat-value">{seaRatio}%</span></div></div>
        <div className="stat-card" style={{ cursor: 'pointer' }} onClick={() => navigate('/purchase-orders')}><Package size={28} /><div><span className="stat-label">PO trong tháng</span><span className="stat-value">{monthlyPoCount}</span></div></div>
      </div>

      <div className="dashboard-panels">
        <section className="dashboard-panel">
          <div className="panel-title">
            <Ship size={20} />
            <h2>Cơ cấu vận chuyển</h2>
          </div>
          <div className="method-mix">
            <div className="mix-bar">
              <span className="mix-sea" style={{ width: `${seaRatio}%` }} />
              <span className="mix-air" style={{ width: `${airRatio}%` }} />
            </div>
            <div className="mix-legend">
              <span><i className="legend-dot sea-dot" /> Sea {seaRatio}%</span>
              <span><i className="legend-dot air-dot" /> Air {airRatio}%</span>
            </div>
          </div>
        </section>

        <section className="dashboard-panel">
          <div className="panel-title">
            <Truck size={20} />
            <h2>Lead time Site</h2>
          </div>
          <div className="leadtime-chart">
            {sites?.slice(0, 6).map((site) => (
              <div className="leadtime-row" key={site.id}>
                <span>{site.code}</span>
                <div>
                  <b className="sea-line" style={{ width: `${Math.max(4, (site.seaLeadTime / maxLeadTime) * 100)}%` }} />
                  <b className="air-line" style={{ width: `${Math.max(4, (site.airLeadTime / maxLeadTime) * 100)}%` }} />
                </div>
              </div>
            ))}
          </div>
        </section>
      </div>
    </>
  );
}
