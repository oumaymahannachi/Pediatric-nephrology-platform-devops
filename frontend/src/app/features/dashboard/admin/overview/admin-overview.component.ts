import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AdminService } from '../../../../core/services/admin.service';
import { LucideAngularModule } from 'lucide-angular';
import { NgApexchartsModule, ApexOptions } from 'ng-apexcharts';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-admin-overview',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule, NgApexchartsModule],
  templateUrl: './admin-overview.component.html',
  styleUrl: './admin-overview.component.scss'
})
export class AdminOverviewComponent implements OnInit {
  loading = true;
  today = new Date();

  stats = {
    totalUsers: 0, doctors: 0, parents: 0, nurses: 0,
    activeUsers: 0, bannedUsers: 0,
    children: 0, measurements: 0, plans: 0, appointments: 0
  };

  cards: { background: string; title: string; icon: string; text: string; number: string; percent: string }[] = [
    { background: 'bg-c-blue',   title: 'Total Users', icon: 'users',       text: 'Registered accounts', number: '0', percent: '' },
    { background: 'bg-c-green',  title: 'Doctors',     icon: 'stethoscope', text: 'Active physicians',   number: '0', percent: '' },
    { background: 'bg-c-yellow', title: 'Parents',     icon: 'heart',       text: 'Family accounts',     number: '0', percent: '' },
    { background: 'bg-c-red',    title: 'Nurses',      icon: 'syringe',     text: 'Nursing staff',       number: '0', percent: '' }
  ];

  recentUsers: any[] = [];

  /* ── Charts ── */
  rolesChart!: Partial<ApexOptions>;
  statusChart!: Partial<ApexOptions>;
  growthChart!: Partial<ApexOptions>;
  barChart!: Partial<ApexOptions>;
  radialChart!: Partial<ApexOptions>;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.initCharts();
    this.loadData();
  }

  private initCharts(): void {
    const months = this.getRecentMonths(6);

    this.growthChart = {
      chart: { height: 260, type: 'area', toolbar: { show: false } },
      series: [{ name: 'Users', data: [0, 0, 0, 0, 0, 0] }],
      xaxis: { categories: months, axisBorder: { show: false }, labels: { style: { colors: '#999', fontSize: '12px' } } },
      yaxis: { show: true, min: 0, labels: { style: { colors: '#999' } } },
      colors: ['#4099ff'],
      dataLabels: { enabled: false },
      stroke: { width: 2.5, curve: 'smooth' },
      fill: { type: 'gradient', gradient: { shadeIntensity: 1, opacityFrom: 0.45, opacityTo: 0.05, stops: [0, 85, 100] } },
      grid: { borderColor: '#f1f1f1', strokeDashArray: 4, padding: { left: 10, right: 10 } },
      tooltip: { theme: 'dark' }
    };

    this.rolesChart = {
      chart: { height: 280, type: 'donut' },
      labels: ['Doctors', 'Parents', 'Nurses'],
      series: [1, 1, 1],
      colors: ['#4099ff', '#2ed8b6', '#ffb64d'],
      plotOptions: { pie: { donut: { size: '72%', labels: { show: true, total: { show: true, label: 'Total', fontSize: '14px', fontWeight: '600' } } } } },
      dataLabels: { enabled: false },
      legend: { position: 'bottom', fontSize: '13px' },
      stroke: { width: 0 },
      tooltip: { theme: 'dark' }
    };

    this.statusChart = {
      chart: { height: 260, type: 'donut' },
      labels: ['Active', 'Banned'],
      series: [1, 0],
      colors: ['#2ed8b6', '#ff5370'],
      plotOptions: { pie: { donut: { size: '72%', labels: { show: true, total: { show: true, label: 'Users', fontSize: '14px', fontWeight: '600' } } } } },
      dataLabels: { enabled: false },
      legend: { position: 'bottom', fontSize: '13px' },
      stroke: { width: 0 },
      tooltip: { theme: 'dark' }
    };

    this.barChart = {
      chart: { height: 260, type: 'bar', toolbar: { show: false } },
      series: [{ name: 'Appointments', data: [0, 0, 0, 0, 0, 0] }],
      xaxis: { categories: months, axisBorder: { show: false }, labels: { style: { colors: '#999', fontSize: '12px' } } },
      colors: ['#7c4dff'],
      plotOptions: { bar: { borderRadius: 4, columnWidth: '45%', distributed: false } },
      dataLabels: { enabled: false },
      grid: { borderColor: '#f1f1f1', strokeDashArray: 4 },
      tooltip: { theme: 'dark' }
    };

    this.radialChart = {
      chart: { height: 260, type: 'radialBar' },
      series: [0, 0, 0],
      labels: ['Users', 'Children', 'Plans'],
      colors: ['#4099ff', '#2ed8b6', '#ffb64d'],
      plotOptions: {
        radialBar: {
          hollow: { size: '30%' },
          track: { background: '#f1f1f1' },
          dataLabels: {
            name: { fontSize: '13px', offsetY: -5 },
            value: { fontSize: '18px', fontWeight: '700', formatter: (val: number) => val.toFixed(0) + '%' },
            total: { show: true, label: 'Health', fontSize: '13px',
              formatter: (w: any) => {
                const avg = w.globals.spikeValues?.length
                  ? w.globals.spikeValues.reduce((a: number, b: number) => a + b, 0) / w.globals.spikeValues.length
                  : 0;
                return Math.round(avg) + '%';
              }
            }
          }
        }
      }
    };
  }

  private loadData(): void {
    this.loading = true;

    forkJoin({
      dashboard: this.adminService.getDashboard(),
      treatment: this.adminService.getTreatmentDashboard(),
      users: this.adminService.getAllUsers()
    }).subscribe({
      next: ({ dashboard, treatment, users }) => {
        // Stats
        this.stats.totalUsers  = dashboard.totalUsers || 0;
        this.stats.doctors     = dashboard.doctorsCount || 0;
        this.stats.parents     = dashboard.parentsCount || 0;
        this.stats.nurses      = dashboard.nursesCount || 0;
        this.stats.activeUsers = users.filter((u: any) => u.status !== 'BANNED').length;
        this.stats.bannedUsers = users.filter((u: any) => u.status === 'BANNED').length;
        this.stats.children     = treatment.childrenCount || 0;
        this.stats.measurements = treatment.measurementsCount || 0;
        this.stats.plans        = treatment.plansCount || 0;
        this.stats.appointments = treatment.appointmentsCount || 0;

        // Cards with percentage
        const total = this.stats.totalUsers || 1;
        this.cards[0].number  = String(this.stats.totalUsers);
        this.cards[0].percent = '100%';
        this.cards[1].number  = String(this.stats.doctors);
        this.cards[1].percent = Math.round(this.stats.doctors * 100 / total) + '%';
        this.cards[2].number  = String(this.stats.parents);
        this.cards[2].percent = Math.round(this.stats.parents * 100 / total) + '%';
        this.cards[3].number  = String(this.stats.nurses);
        this.cards[3].percent = Math.round(this.stats.nurses * 100 / total) + '%';

        // Recent users (last 5, sorted newest first)
        this.recentUsers = [...users]
          .sort((a: any, b: any) => new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime())
          .slice(0, 5);

        // Update charts
        const growthData = this.simulateGrowth(this.stats.totalUsers, 6);
        this.growthChart = { ...this.growthChart, series: [{ name: 'Users', data: growthData }] };
        this.rolesChart  = { ...this.rolesChart,  series: [this.stats.doctors || 1, this.stats.parents || 1, this.stats.nurses || 1] };
        this.statusChart = { ...this.statusChart, series: [this.stats.activeUsers || 1, this.stats.bannedUsers || 0] };
        this.barChart    = { ...this.barChart,    series: [{ name: 'Appointments', data: this.simulateGrowth(this.stats.appointments, 6) }] };

        // Radial: percentages relative to an expected capacity
        const pctUsers    = Math.min(100, Math.round(this.stats.activeUsers * 100 / Math.max(total, 1)));
        const pctChildren = Math.min(100, this.stats.children > 0 ? Math.round(this.stats.children * 100 / Math.max(this.stats.parents, 1)) : 0);
        const pctPlans    = Math.min(100, this.stats.plans > 0 ? Math.round(this.stats.plans * 100 / Math.max(this.stats.children, 1)) : 0);
        this.radialChart  = { ...this.radialChart, series: [pctUsers, pctChildren, pctPlans] };

        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  private getRecentMonths(count: number): string[] {
    const names = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
    const now = new Date();
    const result: string[] = [];
    for (let i = count - 1; i >= 0; i--) {
      const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
      result.push(names[d.getMonth()]);
    }
    return result;
  }

  private simulateGrowth(total: number, months: number): number[] {
    const arr: number[] = [];
    for (let i = 0; i < months; i++) {
      arr.push(Math.round((total * (i + 1)) / months));
    }
    return arr;
  }
}
