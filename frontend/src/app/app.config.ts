import { ApplicationConfig, provideZoneChangeDetection, importProvidersFrom } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { routes } from './app.routes';
import { jwtInterceptor } from './core/interceptors/jwt.interceptor';
import {
  LucideAngularModule,
  ArrowLeft, Award, Baby, BarChart3, Bell, Building2, Calendar, Check,
  ChevronDown, ChevronLeft, ChevronRight, Clipboard, ClipboardList, Edit, Eye, EyeOff, Facebook, FileText, Heart,
  Home, Instagram, LayoutDashboard, Linkedin, Lock, LogOut, Mail, Menu, MessageSquare,
  Pencil, Phone, Plus, Ruler, Save, Settings, Shield, ShieldAlert, ShieldCheck, Star, Stethoscope, TrendingUp,
  Twitter, User, UserCircle, Users, Utensils, X, Activity, Clock, MapPin, AlertCircle, CheckCircle,
  HeartPulse, Syringe, Thermometer, Trash2, Search, Ban, UserCheck, RefreshCw,
  FlaskConical, MessageCircle, Pill, FolderOpen, BarChart2,
  List, CalendarDays, Grid, CheckCircle2, Target, FolderHeart, FlaskRound,
  CalendarCheck, CalendarX, CalendarClock, RotateCcw, ArrowUp, ArrowDown
} from 'lucide-angular';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withInterceptors([jwtInterceptor])),
    provideAnimations(),
    importProvidersFrom(LucideAngularModule.pick({
      ArrowLeft, Award, Baby, BarChart3, Bell, Building2, Calendar, Check,
      ChevronDown, ChevronLeft, ChevronRight, Clipboard, ClipboardList, Edit, Eye, EyeOff, Facebook, FileText, Heart,
      Home, Instagram, LayoutDashboard, Linkedin, Lock, LogOut, Mail, Menu, MessageSquare,
      Pencil, Phone, Plus, Ruler, Save, Settings, Shield, ShieldAlert, ShieldCheck, Star, Stethoscope, TrendingUp,
      Twitter, User, UserCircle, Users, Utensils, X, Activity, Clock, MapPin, AlertCircle, CheckCircle,
      HeartPulse, Syringe, Thermometer, Trash2, Search, Ban, UserCheck, RefreshCw,
      FlaskConical, MessageCircle, Pill, FolderOpen, BarChart2,
      List, CalendarDays, Grid, CheckCircle2, Target, FolderHeart, FlaskRound,
      CalendarCheck, CalendarX, CalendarClock, RotateCcw, ArrowUp, ArrowDown
    }))
  ]
};
