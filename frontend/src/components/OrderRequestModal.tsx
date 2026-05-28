import { useState, useEffect } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { orderRequestService } from '../services/orderRequestService';
import type { OrderRequestResponse, OrderRequestUpsertRequest } from '../services/orderRequestService';
import { catalogService } from '../services/catalogService';
import { X, Plus, Trash } from 'lucide-react';

interface Props {
  isOpen: boolean;
  onClose: () => void;
  requestData: OrderRequestResponse | null;
}

export default function OrderRequestModal({ isOpen, onClose, requestData }: Props) {
  const queryClient = useQueryClient();
  const [expectedDate, setExpectedDate] = useState('');
  const [notes, setNotes] = useState('');
  const [items, setItems] = useState<{ skuId: string; quantity: number; expectedDate: string }[]>([{ skuId: '', quantity: 1, expectedDate: '' }]);

  const { data: skus } = useQuery({
    queryKey: ['skus'],
    queryFn: () => catalogService.listSkus(),
  });

  useEffect(() => {
    if (requestData) {
      setExpectedDate(requestData.expectedDate);
      setNotes(requestData.notes || '');
      if (requestData.items?.length > 0) {
        setItems(requestData.items.map(item => ({ skuId: item.skuId, quantity: item.quantity, expectedDate: item.expectedDate || requestData.expectedDate })));
      } else {
        setItems([{ skuId: '', quantity: 1, expectedDate: '' }]);
      }
    } else {
      setExpectedDate('');
      setNotes('');
      setItems([{ skuId: '', quantity: 1, expectedDate: '' }]);
    }
  }, [requestData]);

  const saveMutation = useMutation({
    mutationFn: (data: OrderRequestUpsertRequest) => {
      if (requestData?.id) {
        return orderRequestService.update(requestData.id, data);
      }
      return orderRequestService.create(data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['order-requests'] });
      onClose();
    },
  });

  const handleSave = () => {
    if (!expectedDate) return alert('Vui lòng chọn ngày cần nhận');
    const validItems = items.filter(i => i.skuId && i.quantity > 0);
    if (validItems.length === 0) return alert('Vui lòng chọn ít nhất 1 linh kiện hợp lệ');

    saveMutation.mutate({
      expectedDate,
      notes,
      items: validItems.map(item => ({ ...item, expectedDate: item.expectedDate || expectedDate })),
    });
  };

  if (!isOpen) return null;

  const isReadOnly = !!requestData && requestData.status !== 'DRAFT';

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h2>{requestData ? (isReadOnly ? 'Chi tiết yêu cầu' : 'Chỉnh sửa yêu cầu') : 'Tạo yêu cầu nhập hàng mới'}</h2>
          <button className="btn-icon" onClick={onClose}><X size={24} /></button>
        </div>
        <div className="modal-body">
          <div className="form-group">
            <label>Ngày cần nhận</label>
            <div className="input-wrapper">
              <input 
                type="date" 
                value={expectedDate} 
                onChange={e => setExpectedDate(e.target.value)} 
                disabled={isReadOnly}
              />
            </div>
          </div>
          <div className="form-group">
            <label>Ghi chú</label>
            <div className="input-wrapper">
              <input 
                placeholder="Mục đích nhập hàng..." 
                value={notes} 
                onChange={e => setNotes(e.target.value)} 
                disabled={isReadOnly}
              />
            </div>
          </div>

          <div>
            <div className="flex-between" style={{ marginBottom: '0.5rem' }}>
              <label style={{ fontWeight: 600, fontSize: '0.9rem' }}>Danh sách linh kiện</label>
                  {!isReadOnly && (
                    <button 
                      className="btn-icon" 
                      style={{ color: 'var(--primary)' }}
                  onClick={() => setItems([...items, { skuId: '', quantity: 1, expectedDate: expectedDate }])}
                >
                  <Plus size={18} /> Thêm linh kiện
                </button>
              )}
            </div>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {items.map((item, idx) => (
                <div key={idx} className="flex-row" style={{ alignItems: 'flex-start' }}>
                  <div className="input-wrapper" style={{ flex: 1 }}>
                    <select 
                      value={item.skuId} 
                      onChange={e => {
                        const newItems = [...items];
                        newItems[idx].skuId = e.target.value;
                        setItems(newItems);
                      }}
                      disabled={isReadOnly}
                    >
                      <option value="">-- Chọn linh kiện --</option>
                      {skus?.map(sku => (
                        <option key={sku.id} value={sku.id}>{sku.code} - {sku.name}</option>
                      ))}
                    </select>
                  </div>
                  <div className="input-wrapper" style={{ width: '120px' }}>
                    <input 
                      type="number" 
                      min="1" 
                      value={item.quantity} 
                      onChange={e => {
                        const newItems = [...items];
                        newItems[idx].quantity = parseInt(e.target.value) || 0;
                        setItems(newItems);
                      }}
                      disabled={isReadOnly}
                    />
                  </div>
                  <div className="input-wrapper" style={{ width: '150px' }}>
                    <input
                      type="date"
                      value={item.expectedDate || expectedDate}
                      onChange={e => {
                        const newItems = [...items];
                        newItems[idx].expectedDate = e.target.value;
                        setItems(newItems);
                      }}
                      disabled={isReadOnly}
                    />
                  </div>
                  {!isReadOnly && items.length > 1 && (
                    <button 
                      className="btn-icon" 
                      onClick={() => setItems(items.filter((_, i) => i !== idx))}
                      style={{ color: 'var(--danger)', marginTop: '0.2rem' }}
                    >
                      <Trash size={18} />
                    </button>
                  )}
                </div>
              ))}
            </div>
          </div>

          {requestData?.cancelReason && (
            <div className="alert alert-error" style={{ marginTop: '1rem' }}>
              <strong>Lý do huỷ:</strong> {requestData.cancelReason}
            </div>
          )}
        </div>
        
        <div className="modal-footer">
          <button className="btn btn-outline" onClick={onClose}>Đóng</button>
          {!isReadOnly && (
            <button 
              className="btn btn-primary" 
              onClick={handleSave} 
              disabled={saveMutation.isPending}
            >
              {saveMutation.isPending ? 'Đang lưu...' : 'Lưu yêu cầu'}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
