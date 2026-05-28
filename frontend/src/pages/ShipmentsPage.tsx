import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Clock, MapPinned, Plus, Ship } from 'lucide-react';
import { purchaseOrderService } from '../services/purchaseOrderService';
import type { POStatus } from '../services/purchaseOrderService';
import { shippingService } from '../services/shippingService';
import { useAuth } from '../hooks/useAuth';

const trackingStatuses: POStatus[] = ['PREPARING', 'SHIPPING', 'ARRIVED'];

export default function ShipmentsPage() {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [selectedPoId, setSelectedPoId] = useState('');
  const [trackingStatus, setTrackingStatus] = useState<POStatus>('SHIPPING');
  const [location, setLocation] = useState('');
  const [notes, setNotes] = useState('');
  const [evidenceFileUrl, setEvidenceFileUrl] = useState('');

  const { data: shipments, isLoading } = useQuery({
    queryKey: ['shipments'],
    queryFn: shippingService.inTransit,
  });

  useEffect(() => {
    if (!selectedPoId && shipments?.[0]) {
      setSelectedPoId(shipments[0].id);
    }
  }, [selectedPoId, shipments]);

  const { data: selectedPo } = useQuery({
    queryKey: ['purchase-order-detail', selectedPoId],
    queryFn: () => purchaseOrderService.detail(selectedPoId),
    enabled: !!selectedPoId,
  });

  const { data: history } = useQuery({
    queryKey: ['tracking-history', selectedPoId],
    queryFn: () => shippingService.history(selectedPoId),
    enabled: !!selectedPoId,
  });

  const addTrackingMutation = useMutation({
    mutationFn: () => shippingService.addTracking(selectedPoId, { status: trackingStatus, location, notes, evidenceFileUrl }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['shipments'] });
      queryClient.invalidateQueries({ queryKey: ['tracking-history', selectedPoId] });
      queryClient.invalidateQueries({ queryKey: ['purchase-order-detail', selectedPoId] });
      setLocation('');
      setNotes('');
      setEvidenceFileUrl('');
    },
  });

  const canUpdateTracking = ['ADMIN', 'OVERSEAS_ORDER', 'SUPPLIER'].includes(user?.role || '');

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault();
    if (!selectedPoId) return;
    addTrackingMutation.mutate();
  };

  return (
    <div>
      <div className="dashboard-header">
        <h1>Theo dõi vận chuyển</h1>
        <p>Cập nhật trạng thái PREPARING, SHIPPING, ARRIVED và lịch sử hành trình của PO.</p>
      </div>

      <div className="split-layout">
        <section className="table-container no-top-margin">
          <table className="data-table">
            <thead>
              <tr>
                <th>PO</th>
                <th>Site</th>
                <th>Phương thức</th>
                <th>Dự kiến đến</th>
                <th>Trạng thái</th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr><td colSpan={5} className="empty-cell">Đang tải vận chuyển...</td></tr>
              ) : shipments?.length === 0 ? (
                <tr><td colSpan={5} className="empty-cell">Không có PO đang vận chuyển.</td></tr>
              ) : (
                shipments?.map((po) => (
                  <tr key={po.id} className={selectedPoId === po.id ? 'selected-row' : ''} onClick={() => setSelectedPoId(po.id)}>
                    <td className="strong-cell">{po.code}</td>
                    <td>{po.siteCode} · {po.siteName}</td>
                    <td>{po.transportMethod === 'SEA' ? 'Sea' : 'Air'}</td>
                    <td>{formatDate(po.expectedArrivalDate)}</td>
                    <td>{formatPoStatus(po.status)}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </section>

        <aside className="side-panel">
          <div className="panel-title">
            <Ship size={20} />
            <h2>{selectedPo?.code || 'Chọn PO'}</h2>
          </div>
          {selectedPo && (
            <div className="site-summary">
              <strong>{selectedPo.siteName}</strong>
              <span>{selectedPo.transportMethod === 'SEA' ? 'Đường biển' : 'Đường hàng không'} · dự kiến {formatDate(selectedPo.expectedArrivalDate)}</span>
              <span className="badge badge-processing">{formatPoStatus(selectedPo.status)}</span>
            </div>
          )}

          {canUpdateTracking && selectedPo && selectedPo.status !== 'COMPLETED' && selectedPo.status !== 'CANCELLED' && (
            <form className="panel-form" onSubmit={handleSubmit}>
              <div className="form-row">
                <div className="form-group">
                  <label>Trạng thái</label>
                  <div className="input-wrapper">
                    <select value={trackingStatus} onChange={(event) => setTrackingStatus(event.target.value as POStatus)}>
                      {trackingStatuses.map((status) => (
                        <option key={status} value={status}>{formatPoStatus(status)}</option>
                      ))}
                    </select>
                  </div>
                </div>
                <div className="form-group">
                  <label>Vị trí</label>
                  <div className="input-wrapper"><input value={location} onChange={(event) => setLocation(event.target.value)} placeholder="Cảng, kho, sân bay..." /></div>
                </div>
              </div>
              <div className="form-group">
                <label>Ghi chú</label>
                <div className="input-wrapper"><input value={notes} onChange={(event) => setNotes(event.target.value)} placeholder="Thông tin cập nhật" /></div>
              </div>
              <div className="form-group">
                <label>Chứng từ</label>
                <div className="input-wrapper"><input value={evidenceFileUrl} onChange={(event) => setEvidenceFileUrl(event.target.value)} placeholder="URL bill/lading hoặc chứng từ" /></div>
              </div>
              <button className="btn btn-primary" type="submit" disabled={addTrackingMutation.isPending}>
                <Plus size={18} /> Thêm cập nhật
              </button>
            </form>
          )}

          <div className="panel-section">
            <h3>Lịch sử tracking</h3>
            <div className="timeline">
              {history?.map((item) => (
                <div className="timeline-item" key={item.id}>
                  <div className="timeline-dot"><MapPinned size={15} /></div>
                  <div>
                    <strong>{formatPoStatus(item.status)}</strong>
                    <span>{item.location || 'Chưa có vị trí'} · {new Date(item.timestamp).toLocaleString('vi-VN')}</span>
                    {item.notes && <p>{item.notes}</p>}
                    {item.evidenceFileUrl && <a href={item.evidenceFileUrl} target="_blank" rel="noreferrer">Chứng từ</a>}
                  </div>
                </div>
              ))}
              {history?.length === 0 && <div className="empty-state"><Clock size={18} /> Chưa có cập nhật tracking.</div>}
            </div>
          </div>
        </aside>
      </div>
    </div>
  );
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

function formatDate(value?: string) {
  return value ? new Date(value).toLocaleDateString('vi-VN') : '-';
}
