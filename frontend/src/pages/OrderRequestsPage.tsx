import { useMemo, useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { orderRequestService } from '../services/orderRequestService';
import type { OrderRequestResponse } from '../services/orderRequestService';
import { useAuth } from '../hooks/useAuth';
import { AlertTriangle, CheckCircle, Cpu, Edit, Eye, PackageCheck, Plus, Search } from 'lucide-react';
import OrderRequestModal from '../components/OrderRequestModal';
import { optimizationService } from '../services/optimizationService';
import type { InventoryCheckResponse, OptimizationResponse } from '../services/optimizationService';
import { purchaseOrderService } from '../services/purchaseOrderService';

export default function OrderRequestsPage() {
  const { user } = useAuth();
  const [statusFilter, setStatusFilter] = useState('');
  const [search, setSearch] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedRequest, setSelectedRequest] = useState<OrderRequestResponse | null>(null);
  const [analysisRequest, setAnalysisRequest] = useState<OrderRequestResponse | null>(null);
  const [inventoryCheck, setInventoryCheck] = useState<InventoryCheckResponse | null>(null);
  const [optimizationResult, setOptimizationResult] = useState<OptimizationResponse | null>(null);
  const [actionError, setActionError] = useState('');
  const queryClient = useQueryClient();

  const { data: requests, isLoading } = useQuery({
    queryKey: ['order-requests', statusFilter, search],
    queryFn: () => orderRequestService.list(statusFilter || undefined, search || undefined),
  });

  const submitMutation = useMutation({
    mutationFn: (id: string) => orderRequestService.submit(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['order-requests'] }),
  });

  const checkMutation = useMutation({
    mutationFn: (id: string) => optimizationService.checkInventory(id),
  });

  const optimizeMutation = useMutation({
    mutationFn: (id: string) => optimizationService.optimize(id),
  });

  const generatePoMutation = useMutation({
    mutationFn: (id: string) => purchaseOrderService.generateFromRequest(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['order-requests'] });
      queryClient.invalidateQueries({ queryKey: ['purchase-orders'] });
      setAnalysisRequest(null);
      setInventoryCheck(null);
      setOptimizationResult(null);
    },
  });

  const canEdit = user?.role === 'SALES' || user?.role === 'ADMIN';
  const canOptimize = user?.role === 'OVERSEAS_ORDER' || user?.role === 'ADMIN';
  const maxAllocation = useMemo(() => {
    return Math.max(1, ...(optimizationResult?.allocations.map((allocation) => allocation.quantity) || [1]));
  }, [optimizationResult]);

  const handleEdit = async (req: OrderRequestResponse) => {
    const fullRequest = await queryClient.fetchQuery({
      queryKey: ['order-request-detail', req.id],
      queryFn: () => orderRequestService.detail(req.id),
    });
    setSelectedRequest(fullRequest);
    setIsModalOpen(true);
  };

  const handleAddNew = () => {
    setSelectedRequest(null);
    setIsModalOpen(true);
  };

  const openAnalysis = (req: OrderRequestResponse) => {
    setAnalysisRequest(req);
    setInventoryCheck(null);
    setOptimizationResult(null);
    setActionError('');
  };

  const handleCheckInventory = (req: OrderRequestResponse) => {
    openAnalysis(req);
    checkMutation.mutate(req.id, {
      onSuccess: setInventoryCheck,
      onError: (error) => setActionError(getErrorMessage(error)),
    });
  };

  const handleOptimize = (req: OrderRequestResponse) => {
    openAnalysis(req);
    optimizeMutation.mutate(req.id, {
      onSuccess: setOptimizationResult,
      onError: (error) => setActionError(getErrorMessage(error)),
    });
  };

  const handleGeneratePo = (requestId: string) => {
    setActionError('');
    generatePoMutation.mutate(requestId, {
      onError: (error) => setActionError(getErrorMessage(error)),
    });
  };

  const formatStatus = (status: string) => {
    const map: Record<string, { label: string, color: string }> = {
      DRAFT: { label: 'Nháp', color: 'badge-draft' },
      PENDING: { label: 'Chờ xử lý', color: 'badge-pending' },
      PROCESSING: { label: 'Đang xử lý', color: 'badge-processing' },
      ORDERED: { label: 'Đã lên đơn', color: 'badge-ordered' },
      CANCELLED: { label: 'Đã huỷ', color: 'badge-cancelled' },
    };
    const s = map[status];
    if (!s) return <span className="badge badge-draft">{status}</span>;
    return <span className={`badge ${s.color}`}>{s.label}</span>;
  };

  return (
    <div>
      <div className="flex-between dashboard-header" style={{ marginBottom: '1.5rem' }}>
        <div>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 700 }}>Yêu cầu nhập hàng</h1>
          <p style={{ color: 'var(--text-muted)' }}>Quản lý các yêu cầu nhập hàng từ các phòng ban</p>
        </div>
        {canEdit && (
          <button className="btn btn-primary" onClick={handleAddNew}>
            <Plus size={18} /> Tạo yêu cầu mới
          </button>
        )}
      </div>

      <div className="flex-row" style={{ marginBottom: '1rem' }}>
        <div className="input-wrapper" style={{ width: '300px' }}>
          <Search size={18} />
          <input placeholder="Tìm kiếm theo mã..." value={search} onChange={e => setSearch(e.target.value)} />
        </div>
        <div className="input-wrapper">
          <select value={statusFilter} onChange={e => setStatusFilter(e.target.value)}>
            <option value="">Tất cả trạng thái</option>
            <option value="DRAFT">Nháp</option>
            <option value="PENDING">Chờ xử lý</option>
            <option value="PROCESSING">Đang xử lý</option>
            <option value="ORDERED">Đã lên đơn</option>
            <option value="CANCELLED">Đã huỷ</option>
          </select>
        </div>
      </div>

      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>Mã YC</th>
              <th>Ngày cần nhận</th>
              <th>Số lượng SKU</th>
              <th>Người tạo</th>
              <th>Trạng thái</th>
              <th style={{ textAlign: 'right' }}>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr><td colSpan={6} style={{ textAlign: 'center', padding: '2rem' }}>Đang tải dữ liệu...</td></tr>
            ) : requests?.length === 0 ? (
              <tr><td colSpan={6} style={{ textAlign: 'center', padding: '2rem' }}>Không tìm thấy yêu cầu nào.</td></tr>
            ) : (
              requests?.map(req => (
                <tr key={req.id}>
                  <td style={{ fontWeight: 600 }}>{req.code}</td>
                  <td>{new Date(req.expectedDate).toLocaleDateString('vi-VN')}</td>
                  <td>{req.itemCount}</td>
                  <td>{req.createdByName}</td>
                  <td>{formatStatus(req.status)}</td>
                  <td style={{ textAlign: 'right' }}>
                    <div className="flex-row" style={{ justifyContent: 'flex-end', gap: '0.5rem' }}>
                      <button className="btn-icon" onClick={() => handleEdit(req)} title="Xem chi tiết"><Eye size={18} /></button>
                      {canOptimize && (req.status === 'PENDING' || req.status === 'PROCESSING') && (
                        <>
                          <button className="btn-icon" onClick={() => handleCheckInventory(req)} title="Khảo sát tồn kho"><Search size={18} /></button>
                          <button className="btn-icon" onClick={() => handleOptimize(req)} title="Chạy thuật toán tối ưu"><Cpu size={18} /></button>
                          <button
                            className="btn-icon"
                            onClick={() => {
                              openAnalysis(req);
                              handleGeneratePo(req.id);
                            }}
                            title="Chốt PO"
                            style={{ color: 'var(--success)' }}
                          >
                            <PackageCheck size={18} />
                          </button>
                        </>
                      )}
                      {req.status === 'DRAFT' && canEdit && (
                        <>
                          <button className="btn-icon" onClick={() => submitMutation.mutate(req.id)} title="Gửi yêu cầu" style={{ color: 'var(--success)' }}><CheckCircle size={18} /></button>
                          <button className="btn-icon" onClick={() => handleEdit(req)} title="Chỉnh sửa"><Edit size={18} /></button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {isModalOpen && (
        <OrderRequestModal
          isOpen={isModalOpen}
          onClose={() => setIsModalOpen(false)}
          requestData={selectedRequest}
        />
      )}

      {analysisRequest && (
        <div className="modal-overlay">
          <div className="modal-content wide-modal">
            <div className="modal-header">
              <div>
                <h2>Tối ưu yêu cầu {analysisRequest.code}</h2>
                <p>Ngày cần nhận: {new Date(analysisRequest.expectedDate).toLocaleDateString('vi-VN')}</p>
              </div>
              <button className="btn-icon" onClick={() => setAnalysisRequest(null)}>×</button>
            </div>
            <div className="modal-body">
              <div className="action-strip">
                <button className="btn btn-outline" onClick={() => handleCheckInventory(analysisRequest)} disabled={checkMutation.isPending}>
                  <Search size={18} /> Khảo sát tồn kho
                </button>
                <button className="btn btn-primary" onClick={() => handleOptimize(analysisRequest)} disabled={optimizeMutation.isPending}>
                  <Cpu size={18} /> Chạy tối ưu
                </button>
                <button className="btn btn-primary" onClick={() => handleGeneratePo(analysisRequest.id)} disabled={generatePoMutation.isPending}>
                  <PackageCheck size={18} /> Chốt PO
                </button>
              </div>

              {actionError && (
                <div className="alert alert-error">
                  <AlertTriangle size={18} />
                  <span>{actionError}</span>
                </div>
              )}

              {inventoryCheck && (
                <section className="result-section">
                  <div className="flex-between">
                    <h3>Nguồn cung khả thi</h3>
                    <span className="result-count">{inventoryCheck.candidates.length} candidate</span>
                  </div>
                  {inventoryCheck.warnings.length > 0 && (
                    <div className="warning-list">
                      {inventoryCheck.warnings.map((warning) => <span key={warning}>{warning}</span>)}
                    </div>
                  )}
                  <div className="table-container compact-table">
                    <table className="data-table">
                      <thead>
                        <tr>
                          <th>SKU</th>
                          <th>Site</th>
                          <th>Tồn kho</th>
                          <th>Phương thức</th>
                          <th>Lead time</th>
                          <th>Dự kiến đến</th>
                        </tr>
                      </thead>
                      <tbody>
                        {inventoryCheck.candidates.map((candidate) => (
                          <tr key={`${candidate.skuId}-${candidate.siteId}-${candidate.transportMethod}`}>
                            <td className="strong-cell">{candidate.skuCode}</td>
                            <td>{candidate.siteCode} · {candidate.siteName}</td>
                            <td>{candidate.availableQuantity.toLocaleString('vi-VN')}</td>
                            <td>{candidate.transportMethod === 'SEA' ? 'Sea' : 'Air'}</td>
                            <td>{candidate.leadTimeDays} ngày</td>
                            <td>{new Date(candidate.expectedArrivalDate).toLocaleDateString('vi-VN')}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </section>
              )}

              {optimizationResult && (
                <section className="result-section">
                  <div className="flex-between">
                    <h3>Phân bổ đề xuất</h3>
                    <span className="result-count">{optimizationResult.allocations.length} dòng PO</span>
                  </div>
                  {optimizationResult.warnings.length > 0 && (
                    <div className="warning-list">
                      {optimizationResult.warnings.map((warning) => <span key={warning}>{warning}</span>)}
                    </div>
                  )}
                  <div className="allocation-list">
                    {optimizationResult.allocations.map((allocation) => (
                      <div className="allocation-row" key={`${allocation.skuId}-${allocation.siteId}-${allocation.transportMethod}`}>
                        <div>
                          <strong>{allocation.skuCode}</strong>
                          <span>{allocation.siteCode} · {allocation.transportMethod === 'SEA' ? 'Sea' : 'Air'} · đến {new Date(allocation.expectedArrivalDate).toLocaleDateString('vi-VN')}</span>
                        </div>
                        <div className="allocation-bar">
                          <span style={{ width: `${Math.max(8, (allocation.quantity / maxAllocation) * 100)}%` }} />
                        </div>
                        <b>{allocation.quantity.toLocaleString('vi-VN')}</b>
                      </div>
                    ))}
                  </div>
                </section>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function getErrorMessage(error: unknown) {
  const apiError = error as { response?: { data?: { message?: string } } };
  return apiError.response?.data?.message || 'Thao tác không thành công';
}
