export interface NavigationItem {
  id: string;
  title: string;
  type: 'item' | 'collapse' | 'group';
  translate?: string;
  icon?: string;
  hidden?: boolean;
  url?: string;
  classes?: string;
  exactMatch?: boolean;
  external?: boolean;
  target?: boolean;
  breadcrumbs?: boolean;

  children?: NavigationItem[];
}
export const NavigationItems: NavigationItem[] = [
  {
    id: 'navigation',
    title: 'My PMS',
    type: 'group',
    icon: 'icon-navigation',
    children: [
      {
        id: 'dashboard',
        title: 'Dashboard',
        type: 'item',
        url: '/dashboard',
        icon: 'feather icon-home',
        classes: 'nav-item'
      },
      {
        id: 'patitents',
        title: 'Patients',
        type: 'item',
        url: '/patients',
        icon: 'feather icon-users',
        classes: 'nav-item'
      },
      {
        id: 'doctors',
        title: 'Doctors',
        type: 'item',
        url: '/doctors',
        icon: 'feather icon-user-check',
        classes: 'nav-item'
      },
    ]
  },
  {
    id: 'appointments',
    title: 'Appointments',
    type: 'group',
    icon: 'feather icon-calendar',
    classes: 'nav-item',
    children: [
      {
        id: 'appointments',
        title: 'Appointments',
        type: 'item',
        url: '/appointments',
        classes: 'nav-item'
      }
    ]
  },
  {
    id: 'chart-maps',
    title: 'Analytics & Maps',
    type: 'group',
    icon: 'icon-analytics',
    children: [
      {
        id: 'chart',
        title: 'Chart',
        type: 'item',
        url: 'apexchart',
        classes: 'nav-item',
        icon: 'feather icon-pie-chart'
      }
    ]
  }, 
  {
    id: 'auth',
    title: 'Authentication',
    type: 'group',
    icon: 'feather icon-lock',
    children: [
      {
        id: 'signup',
        title: 'Sign up',
        type: 'item',
        url: '/register',
        target: true,
        breadcrumbs: false,
        icon: 'feather icon-user-plus'
      },
      {
        id: 'signin',
        title: 'Sign in',
        type: 'item',
        url: '/login',
        icon: 'feather icon-log-in',
        target: true,
        breadcrumbs: false
      }
    ]
  }
];
