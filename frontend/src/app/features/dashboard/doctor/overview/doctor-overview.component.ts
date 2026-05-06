import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { NgApexchartsModule, ApexOptions } from 'ng-apexcharts';
import { forkJoin } from 'rxjs';
import { DoctorService } from '../../../../core/services/doctor.service';
import { Appointment } from '../../../../core/models/appointment.model';
import { Child } from '../../../../core/models/child.model';

@Component({
  selector: 'app-doctor-overview',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule, NgApexchartsModule],
  templateUrl: './doctor-overview.component.html',
  styleUrl: './doctor-overview.component.scss'
})
export class DoctorOverviewComponent implements OnInit {
  loading = true;

  stats = { patients: 0, appointments: 0, pending: 0, completed: 0 };
  patients: Child[] = [];
  appointments: Appointment[] = [];

  cards = [
    { background: 'bg-c-blue', title: 'Total Patients', icon: 'baby', text: 'Active Cases', number: '0' },
    { background: 'bg-c-green', title: 'Appointments', icon: 'calendar', text: 'This Month', number: '0' },
    { background: 'bg-c-yellow', title: 'Pending', icon: 'clock', text: 'Awaiting Response', number: '0' },
    { background: 'bg-c-red', title: 'Completed', icon: 'check-circle', text: 'This Month', number: '0' }
  ];

  /* ApexCharts configs */
  appointmentChart!: Partial<ApexOptions>;
  activityChart!: Partial<ApexOptions>;

  constructor(private doctorService: DoctorService) {}

  ngOnInit(): void {
    this.initCharts();
    this.loadData();
  }

  private initCharts(): void {
    this.appointmentChart = {
      series: [0, 0, 0, 0],
      chart: { type: 'donut', height: 300 },
      labels: ['Completed', 'Accepted', 'Pending', 'Refused'],
      colors: ['#2ed8b6', '#4099ff', '#ffb64d', '#ff5370'],
      legend: { position: 'bottom', fontSize: '13px' },
      plotOptions: {
        pie: {
          donut: { size: '55%', labels: { show: true, total: { show: true, label: 'Total', fontSize: '14px' } } }
        }
      },
      dataLabels: { enabled: false },
      responsive: [{ breakpoint: 480, options: { chart: { height: 260 } } }]
    };

    this.activityChart = {
      series: [{ name: 'Appointments', data: [] }],
      chart: { type: 'area', height: 300, toolbar: { show: false }, zoom: { enabled: false } },
      colors: ['#4099ff'],
      fill: { type: 'gradient', gradient: { shadeIntensity: 1, opacityFrom: 0.4, opacityTo: 0.05, stops: [0, 90, 100] } },
      stroke: { curve: 'smooth', width: 3 },
      xaxis: { categories: this.getRecentMonths(6) },
      yaxis: { min: 0 },
      dataLabels: { enabled: false },
      grid: { borderColor: '#f1f1f1' },
      tooltip: { y: { formatter: (val: number) => val + ' appointments' } }
    };
  }

  private loadData(): void {
    forkJoin({
      dashboard: this.doctorService.getDashboard(),
      patients: this.doctorService.getPatients(),
      appointments: this.doctorService.getAppointments(),
      pending: this.doctorService.getPendingAppointments()
    }).subscribe({
      next: ({ dashboard, patients, appointments, pending }) => {
        this.patients = patients;
        this.appointments = appointments;

        const completed = appointments.filter(a => a.status === 'COMPLETED').length;
        const accepted = appointments.filter(a => a.status === 'ACCEPTED').length;
        const refused = appointments.filter(a => a.status === 'REFUSED').length;
        const pendingCount = pending.length;

        this.stats = {
          patients: dashboard.patientsCount || patients.length,
          appointments: appointments.length,
          pending: pendingCount,
          completed
        };

        this.cards[0].number = String(this.stats.patients);
        this.cards[1].number = String(this.stats.appointments);
        this.cards[2].number = String(this.stats.pending);
        this.cards[3].number = String(completed);

        // Update donut chart
        this.appointmentChart = {
          ...this.appointmentChart,
          series: [completed, accepted, pendingCount, refused]
        };

        // Simulate activity trend
        this.activityChart = {
          ...this.activityChart,
          series: [{ name: 'Appointments', data: this.simulateActivity(appointments.length, 6) }]
        };

        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  private getRecentMonths(count: number): string[] {
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    const now = new Date();
    const result: string[] = [];
    for (let i = count - 1; i >= 0; i--) {
      const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
      result.push(months[d.getMonth()]);
    }
    return result;
  }

  private simulateActivity(total: number, months: number): number[] {
    const data: number[] = [];
    let running = Math.max(1, Math.floor(total * 0.3));
    for (let i = 0; i < months; i++) {
      running = Math.max(0, running + Math.floor(Math.random() * 4) - 1);
      data.push(running);
    }
    data[months - 1] = total;
    return data;
  }
}
