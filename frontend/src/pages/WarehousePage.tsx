import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { ClipboardCheck, PackageOpen, Warehouse } from 'lucide-react';
import { purchaseOrderService } from '../services/purchaseOrderService';
import { warehouseService } from '../services/warehouseService';

interface ReceiveLine {
  quantityReceived: number;
  notes: string;
}

export default function WarehousePage() {
  const queryClient = useQueryClient();
  const [selectedPoId, setSelectedPoId] = useState('');
  const [actualArrivalDate, setActualArrivalDate] = useState(() => new Date().toISOString().slice(0, 10));
  const [receiveLines, setReceiveLines] = useState<Record<string, ReceiveLine>>({});

  const { data: inboundOrders, isLoading } = useQuery({
    queryKey: ['warehouse-inbound'],
    queryFn: warehouseService.inbound,
  });

  useEffect(() => {
    if (!selectedPoId && inboundOrders?.[0]) {
      setSelectedPoId(inboundOrders[0].id);
    }
  }, [selectedPoId, inboundOrders]);

  const { data: selectedPo } = useQuery({
    queryKey: ['purchase-order-detail', selectedPoId],
    queryFn: () => purchaseOrderService.detail(selectedPoId),
    enabled: !!selectedPoId,
  });

  useEffect(() => {
    if (selectedPo) {
      const nextLines: Record<string, ReceiveLine> = {};
      selectedPo.items.forEach((item) => {
        nextLines[item.id] = {
          quantityReceived: item.quantityReceived || item.quantityOrdered,
          notes: item.notes || '',
        };
      });
      setReceiveLines(nextLines);
    }
  }, [selectedPo]);

  const receiveMutation = useMutation({
    mutationFn: () => warehouseService.receive(selectedPoId, {
      actualArrivalDate,
      items: Object.entries(receiveLines).map(([purchaseOrderItemId, line]) => ({
        purchaseOrderItemId,
        quantityReceived: line.quantityReceived,
        notes: line.notes,
      })),
    }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['warehouse-inbound'] });
      queryClient.invalidateQueries({ queryKey: ['purchase-order-detail'] });
      queryClient.invalidateQueries({ queryKey: ['purchase-orders'] });
      setSelectedPoId('');
    },
  });

  const handleReceive = (event: FormEvent) => {
    event.preventDefault();
    if (!selectedPo) return;
    const missingNotes = selectedPo.items.some((item) => {
      const line = receiveLines[item.id];
      return line && line.quantityReceived !== item.quantityOrdered && !line.notes.trim();
    });
    if (missingNotes) {
      window.alert('Vui lòng nhập ghi chú cho dòng hàng có chênh lệch.');
      return;
    }
    receiveMutation.mutate();
  };

  return (
    <div>
      <div className="dashboard-header">
        <h1>Nhập kho</h1>
        <p>Đối chiếu PO đã vận chuyển hoặc đã đến và ghi nhận số lượng thực nhận.</p>
      </div>

      <div className="split-layout">
        <section className="table-container no-top-margin">
          <table className="data-table">
            <thead>
              <tr>
                <th>PO</th>
                <th>Site</th>
                <th>Dự kiến đến</th>
                <th>Trạng thái</th>
                <th>Số dòng</th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr><td colSpan={5} className="empty-cell">Đang tải danh sách nhập kho...</td></tr>
              ) : inboundOrders?.length === 0 ? (
                <tr><td colSpan={5} className="empty-cell">Không có PO cần nhập kho.</td></tr>
              ) : (
                inboundOrders?.map((po) => (
                  <tr key={po.id} className={selectedPoId === po.id ? 'selected-row' : ''} onClick={() => setSelectedPoId(po.id)}>
                    <td className="strong-cell">{po.code}</td>
                    <td>{po.siteCode} · {po.siteName}</td>
                    <td>{formatDate(po.expectedArrivalDate)}</td>
                    <td>{po.status === 'ARRIVED' ? 'Đã đến' : 'Đang vận chuyển'}</td>
                    <td>{po.itemCount}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </section>

        <aside className="side-panel">
          <div className="panel-title">
            <Warehouse size={20} />
            <h2>{selectedPo?.code || 'Chọn PO'}</h2>
          </div>
          {selectedPo ? (
            <form onSubmit={handleReceive} className="panel-form">
              <div className="form-group">
                <label>Ngày thực nhận</label>
                <div className="input-wrapper">
                  <input type="date" value={actualArrivalDate} onChange={(event) => setActualArrivalDate(event.target.value)} />
                </div>
              </div>

              <div className="receive-list">
                {selectedPo.items.map((item) => {
                  const line = receiveLines[item.id] || { quantityReceived: item.quantityOrdered, notes: '' };
                  const difference = line.quantityReceived - item.quantityOrdered;
                  return (
                    <div className="receive-row" key={item.id}>
                      <div>
                        <strong>{item.skuCode}</strong>
                        <span>{item.skuName}</span>
                      </div>
                      <div className="receive-controls">
                        <span>Đặt {item.quantityOrdered.toLocaleString('vi-VN')} {item.unit}</span>
                        <div className="input-wrapper">
                          <input
                            type="number"
                            min="0"
                            value={line.quantityReceived}
                            onChange={(event) => setReceiveLines({
                              ...receiveLines,
                              [item.id]: { ...line, quantityReceived: Number(event.target.value) },
                            })}
                          />
                        </div>
                      </div>
                      {difference !== 0 && <span className="danger-text">Chênh lệch {difference.toLocaleString('vi-VN')}</span>}
                      <div className="input-wrapper">
                        <input
                          value={line.notes}
                          onChange={(event) => setReceiveLines({
                            ...receiveLines,
                            [item.id]: { ...line, notes: event.target.value },
                          })}
                          placeholder="Ghi chú khi có chênh lệch"
                        />
                      </div>
                    </div>
                  );
                })}
              </div>

              <button className="btn btn-primary" type="submit" disabled={receiveMutation.isPending}>
                <ClipboardCheck size={18} /> Chốt nhập kho
              </button>
            </form>
          ) : (
            <div className="empty-state"><PackageOpen size={18} /> Chưa chọn PO nhập kho.</div>
          )}
        </aside>
      </div>
    </div>
  );
}

function formatDate(value?: string) {
  return value ? new Date(value).toLocaleDateString('vi-VN') : '-';
}
