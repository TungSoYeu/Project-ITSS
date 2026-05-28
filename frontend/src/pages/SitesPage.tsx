import { useEffect, useMemo, useState } from 'react';
import type { FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Edit, MapPin, Plus, Save, Search } from 'lucide-react';
import { catalogService } from '../services/catalogService';
import type { SiteRequest, SiteResponse } from '../services/catalogService';
import { useAuth } from '../hooks/useAuth';

const emptySiteForm: SiteRequest = {
  code: '',
  name: '',
  country: '',
  seaLeadTime: 1,
  airLeadTime: 1,
  active: true,
};

export default function SitesPage() {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [search, setSearch] = useState('');
  const [activeFilter, setActiveFilter] = useState('');
  const [selectedSiteId, setSelectedSiteId] = useState('');
  const [siteForm, setSiteForm] = useState<SiteRequest>(emptySiteForm);
  const [editingSiteId, setEditingSiteId] = useState('');
  const [inventorySkuId, setInventorySkuId] = useState('');
  const [inventoryQuantity, setInventoryQuantity] = useState(0);

  const activeValue = activeFilter === '' ? undefined : activeFilter === 'true';
  const canManage = user?.role === 'ADMIN' || user?.role === 'OVERSEAS_ORDER';
  const canUpdateInventory = canManage || user?.role === 'WAREHOUSE';

  const { data: sites, isLoading } = useQuery({
    queryKey: ['sites', activeValue, search],
    queryFn: () => catalogService.listSites(activeValue, search || undefined),
  });

  const selectedSite = useMemo(
    () => sites?.find((site) => site.id === selectedSiteId) || sites?.[0],
    [selectedSiteId, sites],
  );

  useEffect(() => {
    if (!selectedSiteId && sites?.[0]) {
      setSelectedSiteId(sites[0].id);
    }
  }, [selectedSiteId, sites]);

  const { data: inventory } = useQuery({
    queryKey: ['site-inventory', selectedSite?.id],
    queryFn: () => catalogService.getSiteInventory(selectedSite?.id || ''),
    enabled: !!selectedSite?.id,
  });

  const { data: skus } = useQuery({
    queryKey: ['skus'],
    queryFn: () => catalogService.listSkus(),
  });

  const saveSiteMutation = useMutation({
    mutationFn: (data: SiteRequest) => editingSiteId ? catalogService.updateSite(editingSiteId, data) : catalogService.createSite(data),
    onSuccess: (site) => {
      queryClient.invalidateQueries({ queryKey: ['sites'] });
      setSelectedSiteId(site.id);
      setEditingSiteId('');
      setSiteForm(emptySiteForm);
    },
  });

  const inventoryMutation = useMutation({
    mutationFn: () => catalogService.upsertInventory(selectedSite?.id || '', inventorySkuId, inventoryQuantity),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['site-inventory'] });
      queryClient.invalidateQueries({ queryKey: ['sites'] });
      setInventorySkuId('');
      setInventoryQuantity(0);
    },
  });

  const startEditSite = (site: SiteResponse) => {
    setEditingSiteId(site.id);
    setSiteForm({
      code: site.code,
      name: site.name,
      country: site.country,
      seaLeadTime: site.seaLeadTime,
      airLeadTime: site.airLeadTime,
      active: site.active,
    });
  };

  const handleSiteSubmit = (event: FormEvent) => {
    event.preventDefault();
    saveSiteMutation.mutate(siteForm);
  };

  const handleInventorySubmit = (event: FormEvent) => {
    event.preventDefault();
    if (!inventorySkuId || !selectedSite) return;
    inventoryMutation.mutate();
  };

  return (
    <div>
      <div className="flex-between dashboard-header">
        <div>
          <h1>Site & tồn kho</h1>
          <p>Quản lý nhà cung cấp, lead time Sea/Air và tồn kho khả dụng theo SKU.</p>
        </div>
        {canManage && (
          <button
            className="btn btn-primary"
            onClick={() => {
              setEditingSiteId('');
              setSiteForm(emptySiteForm);
            }}
          >
            <Plus size={18} /> Site mới
          </button>
        )}
      </div>

      <div className="split-layout">
        <section>
          <div className="toolbar">
            <div className="input-wrapper toolbar-search">
              <Search size={18} />
              <input placeholder="Tìm mã site, tên, quốc gia..." value={search} onChange={(event) => setSearch(event.target.value)} />
            </div>
            <div className="input-wrapper">
              <select value={activeFilter} onChange={(event) => setActiveFilter(event.target.value)}>
                <option value="">Tất cả</option>
                <option value="true">Đang hoạt động</option>
                <option value="false">Tạm ngưng</option>
              </select>
            </div>
          </div>

          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Site</th>
                  <th>Quốc gia</th>
                  <th>Sea</th>
                  <th>Air</th>
                  <th>SKU</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {isLoading ? (
                  <tr><td colSpan={6} className="empty-cell">Đang tải Site...</td></tr>
                ) : sites?.length === 0 ? (
                  <tr><td colSpan={6} className="empty-cell">Chưa có Site phù hợp.</td></tr>
                ) : (
                  sites?.map((site) => (
                    <tr key={site.id} className={selectedSite?.id === site.id ? 'selected-row' : ''} onClick={() => setSelectedSiteId(site.id)}>
                      <td><span className="code-chip">{site.code}</span> {site.name}</td>
                      <td>{site.country}</td>
                      <td>{site.seaLeadTime} ngày</td>
                      <td>{site.airLeadTime} ngày</td>
                      <td>{site.skuCount}</td>
                      <td style={{ textAlign: 'right' }}>
                        {canManage && <button className="btn-icon" onClick={() => startEditSite(site)} title="Sửa Site"><Edit size={18} /></button>}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </section>

        <aside className="side-panel">
          <div className="panel-title">
            <MapPin size={20} />
            <h2>{selectedSite ? selectedSite.code : 'Chọn Site'}</h2>
          </div>
          {selectedSite && (
            <div className="site-summary">
              <strong>{selectedSite.name}</strong>
              <span>{selectedSite.country} · Sea {selectedSite.seaLeadTime} ngày · Air {selectedSite.airLeadTime} ngày</span>
              {selectedSite.active ? <span className="badge badge-ordered">Hoạt động</span> : <span className="badge badge-cancelled">Tạm ngưng</span>}
            </div>
          )}

          {canManage && (
            <form className="panel-form" onSubmit={handleSiteSubmit}>
              <div className="form-row">
                <div className="form-group">
                  <label>Mã Site</label>
                  <div className="input-wrapper"><input value={siteForm.code} onChange={(event) => setSiteForm({ ...siteForm, code: event.target.value })} required /></div>
                </div>
                <div className="form-group">
                  <label>Quốc gia</label>
                  <div className="input-wrapper"><input value={siteForm.country} onChange={(event) => setSiteForm({ ...siteForm, country: event.target.value })} required /></div>
                </div>
              </div>
              <div className="form-group">
                <label>Tên Site</label>
                <div className="input-wrapper"><input value={siteForm.name} onChange={(event) => setSiteForm({ ...siteForm, name: event.target.value })} required /></div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Sea lead time</label>
                  <div className="input-wrapper"><input type="number" min="0" value={siteForm.seaLeadTime} onChange={(event) => setSiteForm({ ...siteForm, seaLeadTime: Number(event.target.value) })} required /></div>
                </div>
                <div className="form-group">
                  <label>Air lead time</label>
                  <div className="input-wrapper"><input type="number" min="0" value={siteForm.airLeadTime} onChange={(event) => setSiteForm({ ...siteForm, airLeadTime: Number(event.target.value) })} required /></div>
                </div>
              </div>
              <label className="toggle-row">
                <input type="checkbox" checked={siteForm.active} onChange={(event) => setSiteForm({ ...siteForm, active: event.target.checked })} />
                Đang hoạt động
              </label>
              <button className="btn btn-primary" type="submit" disabled={saveSiteMutation.isPending}>
                <Save size={18} /> {editingSiteId ? 'Lưu Site' : 'Tạo Site'}
              </button>
            </form>
          )}

          <div className="panel-section">
            <h3>Tồn kho Site</h3>
            {canUpdateInventory && selectedSite && (
              <form className="inventory-form" onSubmit={handleInventorySubmit}>
                <div className="input-wrapper">
                  <select value={inventorySkuId} onChange={(event) => setInventorySkuId(event.target.value)} required>
                    <option value="">Chọn SKU</option>
                    {skus?.map((sku) => (
                      <option key={sku.id} value={sku.id}>{sku.code} - {sku.name}</option>
                    ))}
                  </select>
                </div>
                <div className="input-wrapper quantity-input">
                  <input type="number" min="0" value={inventoryQuantity} onChange={(event) => setInventoryQuantity(Number(event.target.value))} required />
                </div>
                <button className="btn btn-outline" type="submit" disabled={inventoryMutation.isPending}>Cập nhật</button>
              </form>
            )}
            <div className="inventory-list">
              {inventory?.map((item) => (
                <div className="inventory-row" key={item.id}>
                  <div>
                    <strong>{item.skuCode}</strong>
                    <span>{item.skuName}</span>
                  </div>
                  <b>{item.quantity.toLocaleString('vi-VN')} {item.unit}</b>
                </div>
              ))}
              {inventory?.length === 0 && <div className="empty-state">Site chưa có tồn kho.</div>}
            </div>
          </div>
        </aside>
      </div>
    </div>
  );
}
