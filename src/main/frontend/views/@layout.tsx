import { AppLayout } from '@vaadin/react-components';
import { Outlet } from 'react-router';

export default function MainLayout() {
  return (
    <AppLayout primarySection="drawer">
      <Outlet />
    </AppLayout>
  );
}
