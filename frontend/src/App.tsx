import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from './hooks/useAuth';
import PrivateRoute from './routes/PrivateRoute';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';

import MainLayout from './layouts/MainLayout';

import OrderRequestsPage from './pages/OrderRequestsPage';
import PurchaseOrdersPage from './pages/PurchaseOrdersPage';
import ShipmentsPage from './pages/ShipmentsPage';
import SitesPage from './pages/SitesPage';
import WarehousePage from './pages/WarehousePage';
import ProfilePage from './pages/ProfilePage';

const queryClient = new QueryClient({ defaultOptions: { queries: { staleTime: 5 * 60 * 1000, retry: 1 } } });

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/dashboard" element={<PrivateRoute><MainLayout><DashboardPage /></MainLayout></PrivateRoute>} />
            <Route path="/order-requests" element={<PrivateRoute><MainLayout><OrderRequestsPage /></MainLayout></PrivateRoute>} />
            <Route path="/profile" element={<PrivateRoute><MainLayout><ProfilePage /></MainLayout></PrivateRoute>} />
            <Route path="/purchase-orders" element={<PrivateRoute><MainLayout><PurchaseOrdersPage /></MainLayout></PrivateRoute>} />
            <Route path="/sites" element={<PrivateRoute><MainLayout><SitesPage /></MainLayout></PrivateRoute>} />
            <Route path="/shipments" element={<PrivateRoute><MainLayout><ShipmentsPage /></MainLayout></PrivateRoute>} />
            <Route path="/warehouse" element={<PrivateRoute><MainLayout><WarehousePage /></MainLayout></PrivateRoute>} />
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  );
}

export default App;
