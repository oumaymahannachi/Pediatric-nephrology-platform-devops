import { Component } from '@angular/core';
import { NavbarComponent } from './components/navbar/navbar.component';
import { HeroComponent } from './components/hero/hero.component';
import { TrustBarComponent } from './components/trust-bar/trust-bar.component';
import { FeaturesComponent } from './components/features/features.component';
import { HowItWorksComponent } from './components/how-it-works/how-it-works.component';
import { TestimonialsComponent } from './components/testimonials/testimonials.component';
import { PricingComponent } from './components/pricing/pricing.component';
import { FaqComponent } from './components/faq/faq.component';
import { CtaBannerComponent } from './components/cta-banner/cta-banner.component';
import { FooterComponent } from './components/footer/footer.component';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [
    NavbarComponent,
    HeroComponent,
    TrustBarComponent,
    FeaturesComponent,
    HowItWorksComponent,
    TestimonialsComponent,
    PricingComponent,
    FaqComponent,
    CtaBannerComponent,
    FooterComponent
  ],
  template: `
    <app-navbar />
    <app-hero />
    <app-trust-bar />
    <app-features />
    <app-how-it-works />
    <app-testimonials />
    <app-pricing />
    <app-faq />
    <app-cta-banner />
    <app-footer />
  `,
  styles: [`:host { display: block; }`]
})
export class LandingComponent {}
