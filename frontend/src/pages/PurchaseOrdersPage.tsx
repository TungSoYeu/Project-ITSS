import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Eye, PackageCheck, Search, Ship, XCircle } from 'lucide-react';
import { catalogService } from '../services/catalogService';
import { purchaseOrderService } from '../services/purchaseOrderService';
import type { POStatus, PurchaseOrderResponse } from '../services/purchaseOrderService';
import { useAuth } from '../hooks/useAuth';

const poStatuses: POStatus[] = ['PENDING_CONFIRM', 'PREPARING', 'SHIPPING', 'ARRIVED', 'COMPLETED', 'CANCELLED'];

export default function PurchaseOrdersPage() {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [statusFilter, setStatusFilter] = useState<POStatus | ''>('');
  const [siteFilter, setSiteFilter] = useState('');
  const [search, setSearch] = useState('');
  const [selectedId, setSelectedId] = useState('');

  const { data: purchaseOrders, isLoading } = useQuery({
    queryKey: ['purchase-orders', statusFilter, siteFilter, search],
    queryFn: () => purchaseOrderService.list(statusFilter, siteFilter || undefined, search || undefined),
  });

  const { data: sites } = useQuery({
    queryKey: ['sites', 'active'],
    queryFn: () => catalogService.listSites(true),
  });

  const { data: selectedPo } = useQuery({
    queryKey: ['purchase-order-detail', selectedId],
    queryFn: () => purchaseOrderService.detail(selectedId),
    enabled: !!selectedId,
  });

  const updateStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: string; status: POStatus }) => purchaseOrderService.updateStatus(id, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['purchase-orders'] });
      queryClient.invalidateQueries({ queryKey: ['purchase-order-detail'] });
      queryClient.invalidateQueries({ queryKey: ['shipments'] });
      queryClient.invalidateQueries({ queryKey: ['warehouse-inbound'] });
    },
  });

  const cancelMutation = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) => purchaseOrderService.cancel(id, reason),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['purchase-orders'] });
      queryClient.invalidateQueries({ queryKey: ['purchase-order-detail'] });
    },
  });

  const totals = useMemo(() => summarizeOrders(purchaseOrders || []), [purchaseOrders]);
  const canOperatePo = ['ADMIN', 'OVERSEAS_ORDER', 'SUPPLIER', 'WAREHOUSE'].includes(user?.role || '');
  const canCancel = user?.role === 'ADMIN' || user?.role === 'OVERSEAS_ORDER';

  const handleCancel = (po: PurchaseOrderResponse) => {
    const reason = window.prompt(`Nhập lý do huỷ ${po.code}`);
    if (reason?.trim()) {
      cancelMutation.mutate({ id: po.id, reason: reason.trim() });
    }
  };

  return (
    <div>
      <div className="flex-between dashboard-header">
        <div>
          <h1>Đơn đặt hàng</h1>
          <p>Theo dõi PO đã sinh từ thuật toán tối ưu và trạng thái xác nhận với Site.</p>
        </div>
        <div className="summary-pills">
          <span>{totals.total} PO</span>
          <span>{totals.seaRatio}% Sea</span>
        </div>
      </div>

      <div className="toolbar">
        <div className="input-wrapper toolbar-search">
          <Search size={18} />
          <input placeholder="Tìm PO, yêu cầu, site, SKU..." value={search} onChange={(event) => setSearch(event.target.value)} />
        </div>
        <div className="input-wrapper">
          <select value={statusFilter} onChange={(event) => setStatusFilter(event.target.value as POStatus | '')}>
            <option value="">Tất cả trạng thái</option>
            {poStatuses.map((status) => (
              <option key={status} value={status}>{formatPoStatus(status)}</option>
            ))}
          </select>
        </div>
        <div className="input-wrapper">
          <select value={siteFilter} onChange={(event) => setSiteFilter(event.target.value)}>
            <option value="">Tất cả Site</option>
            {sites?.map((site) => (
              <option key={site.id} value={site.id}>{site.code}</option>
            ))}
          </select>
        </div>
      </div>

      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>Mã PO</th>
              <th>Yêu cầu</th>
              <th>Site</th>
              <th>Phương thức</th>
              <th>Ngày dự kiến</th>
              <th>Trạng thái</th>
              <th style={{ textAlign: 'right' }}>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr><td colSpan={7} className="empty-cell">Đang tải PO...</td></tr>
            ) : purchaseOrders?.length === 0 ? (
              <tr><td colSpan={7} className="empty-cell">Chưa có PO phù hợp.</td></tr>
            ) : (
              purchaseOrders?.map((po) => (
                <tr key={po.id}>
                  <td className="strong-cell">{po.code}</td>
                  <td>{po.requestCode}</td>
                  <td><span className="code-chip">{po.siteCode}</span> {po.siteName}</td>
                  <td>{formatTransport(po.transportMethod)}</td>
                  <td>{formatDate(po.expectedArrivalDate)}</td>
                  <td>{renderPoBadge(po.status)}</td>
                  <td style={{ textAlign: 'right' }}>
                    <div className="table-actions">
                      <button className="btn-icon" onClick={() => setSelectedId(po.id)} title="Xem chi tiết"><Eye size={18} /></button>
                      {canOperatePo && nextStatus(po.status) && (
                        <button
                          className="btn-icon"
                          onClick={() => updateStatusMutation.mutate({ id: po.id, status: nextStatus(po.status) as POStatus })}
                          title={`Chuyển sang ${formatPoStatus(nextStatus(po.status) as POStatus)}`}
                        >
                          <PackageCheck size={18} />
                        </button>
                      )}
                      {canCancel && po.status !== 'CANCELLED' && po.status !== 'COMPLETED' && (
                        <button className="btn-icon danger-icon" onClick={() => handleCancel(po)} title="Huỷ PO"><XCircle size={18} /></button>
                      )}
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {selectedPo && (
        <section className="detail-panel">
          <div className="flex-between detail-header">
            <div>
              <h2>{selectedPo.code}</h2>
              <p>{selectedPo.siteName} · {formatTransport(selectedPo.transportMethod)} · dự kiến {formatDate(selectedPo.expectedArrivalDate)}</p>
            </div>
            {renderPoBadge(selectedPo.status)}
          </div>

          <div className="detail-grid">
            <div>
              <span className="detail-label">Yêu cầu gốc</span>
              <strong>{selectedPo.requestCode}</strong>
            </div>
            <div>
              <span className="detail-label">Người tạo PO</span>
              <strong>{selectedPo.createdByName}</strong>
            </div>
            <div>
              <span className="detail-label">Ngày thực nhận</span>
              <strong>{selectedPo.actualArrivalDate ? formatDate(selectedPo.actualArrivalDate) : '-'}</strong>
            </div>
          </div>

          <div className="table-container compact-table">
            <table className="data-table">
              <thead>
                <tr>
                  <th>SKU</th>
                  <th>Tên hàng</th>
                  <th>Đặt</th>
                  <th>Thực nhận</th>
                  <th>Chênh lệch</th>
                </tr>
              </thead>
              <tbody>
                {selectedPo.items.map((item) => (
                  <tr key={item.id}>
                    <td className="strong-cell">{item.skuCode}</td>
                    <td>{item.skuName}</td>
                    <td>{item.quantityOrdered.toLocaleString('vi-VN')} {item.unit}</td>
                    <td>{item.quantityReceived.toLocaleString('vi-VN')}</td>
                    <td className={item.difference === 0 ? '' : 'danger-text'}>{item.difference.toLocaleString('vi-VN')}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {selectedPo.cancelReason && (
            <div className="alert alert-error">Lý do huỷ: {selectedPo.cancelReason}</div>
          )}
        </section>
      )}
    </div>
  );
}

function summarizeOrders(orders: PurchaseOrderResponse[]) {
  const total = orders.length;
  const sea = orders.filter((order) => order.transportMethod === 'SEA').length;
  return { total, seaRatio: total === 0 ? 0 : Math.round((sea / total) * 100) };
}

function nextStatus(status: POStatus): POStatus | null {
  if (status === 'PENDING_CONFIRM') return 'PREPARING';
  if (status === 'PREPARING') return 'SHIPPING';
  if (status === 'SHIPPING') return 'ARRIVED';
  return null;
}

function formatPoStatus(status: POStatus) {
  const map: Record<POStatus, string> = {
    PENDING_CONFIRM: 'Chờ xác nhận',
    PREPARING: 'Đang chuẩn bị',
    SHIPPING: 'Đang vận chuyển',
    ARRIVED: 'Đã đến',
    COMPLETED: 'Hoàn tất',
    CANCELLED: 'Đã huỷ',
  };
  return map[status];
}

function renderPoBadge(status: POStatus) {
  const classMap: Record<POStatus, string> = {
    PENDING_CONFIRM: 'badge-pending',
    PREPARING: 'badge-processing',
    SHIPPING: 'badge-processing',
    ARRIVED: 'badge-ordered',
    COMPLETED: 'badge-ordered',
    CANCELLED: 'badge-cancelled',
  };
  return <span className={`badge ${classMap[status]}`}>{formatPoStatus(status)}</span>;
}

function formatTransport(method: string) {
  return method === 'SEA' ? <span className="method method-sea"><Ship size={15} /> Đường biển</span> : <span className="method method-air">Air</span>;
}

function formatDate(value?: string) {
  return value ? new Date(value).toLocaleDateString('vi-VN') : '-';
}
