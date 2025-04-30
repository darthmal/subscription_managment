import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
  imports: [CommonModule, RouterLink],
  standalone: true
})
export class HomeComponent {
  features = [
    {
      title: 'Smart Form',
      description: 'Intuitive 5-step registration process with auto-save functionality',
      icon: 'document-text'
    },
    {
      title: 'Document Upload',
      description: 'Secure PDF/IMG uploads with preview and validation',
      icon: 'cloud-upload'
    },
    {
      title: 'Notifications',
      description: 'Stay updated with progress alerts and reminders',
      icon: 'bell'
    },
    {
      title: 'Secure Authentication',
      description: 'Multi-auth options with email, Google, and Microsoft',
      icon: 'shield-check'
    }
  ];
}
