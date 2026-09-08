import type { ReactNode } from 'react';
import { useNavigation } from 'react-router-dom';
import Header from './Header';
import Footer from './Footer';

type LayoutProps = {
  children?: ReactNode;
};

export default function Layout({ children }: LayoutProps) {
  const navigation = useNavigation();
  const isNavigating = navigation.state === 'loading';

  return (
    <div className="d-flex flex-column min-vh-100 bg-body-tertiary">
      <Header />
      {isNavigating && (
        <div className="progress rounded-0" style={{ height: '3px' }} role="status" aria-label="Caricamento">
          <div className="progress-bar progress-bar-striped progress-bar-animated w-100" />
        </div>
      )}
      <main className="container py-4 flex-grow-1">{children}</main>
      <Footer />
    </div>
  );
}
