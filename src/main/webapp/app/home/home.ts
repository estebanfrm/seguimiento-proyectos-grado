import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { AccountService } from 'app/core/auth';
import { TranslateDirective } from 'app/shared/language';

@Component({
  selector: 'jhi-home',
  templateUrl: './home.html',
  styleUrl: './home.scss',
  imports: [TranslateDirective, RouterLink],
})
export default class Home {
  public readonly account = inject(AccountService).account;
  readonly textoSeguimiento = computed(() => {
    const authorities = this.account()?.authorities ?? [];
    const rol = ['ADMIN', 'ESTUDIANTE', 'TUTOR', 'JURADO'].find(authority => authorities.includes(`ROLE_${authority}`));
    return `seguimiento.home.${rol ?? 'USER'}`;
  });

  private readonly router = inject(Router);

  login(): void {
    this.router.navigate(['/login']);
  }
}
