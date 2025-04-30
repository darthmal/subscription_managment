import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-auth-layout',
  templateUrl: './auth-layout.component.html',
  styleUrl: './auth-layout.component.scss',
  imports: [RouterOutlet],
  standalone: true
})
export class AuthLayoutComponent {
  currentYear = new Date().getFullYear();
}
