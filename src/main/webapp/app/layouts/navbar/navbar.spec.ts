import { beforeEach, describe, expect, it, vi } from 'vitest';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { provideTranslateService } from '@ngx-translate/core';
import { FaIconLibrary } from '@fortawesome/angular-fontawesome';
import { of } from 'rxjs';

import { Account, AccountService } from 'app/core/auth';
import { fontAwesomeIcons } from 'app/config/font-awesome-icons';
import { ProfileInfo } from 'app/layouts/profiles/profile-info.model';
import { ProfileService } from 'app/layouts/profiles/profile.service';
import { LoginService } from 'app/login/login.service';

import Navbar from './navbar';

describe('Navbar Component', () => {
  let comp: Navbar;
  let fixture: ComponentFixture<Navbar>;
  let accountService: AccountService;
  let profileService: ProfileService;
  const account: Account = {
    activated: true,
    authorities: [],
    email: '',
    firstName: 'John',
    langKey: '',
    lastName: 'Doe',
    login: 'john.doe',
    imageUrl: '',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideRouter([]), provideTranslateService(), provideHttpClientTesting(), LoginService],
    });
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(Navbar);
    comp = fixture.componentInstance;
    accountService = TestBed.inject(AccountService);
    profileService = TestBed.inject(ProfileService);
    TestBed.inject(FaIconLibrary).addIcons(...fontAwesomeIcons);
  });

  it('should call profileService.getProfileInfo on init', () => {
    // GIVEN
    vi.spyOn(profileService, 'getProfileInfo').mockReturnValue(of(new ProfileInfo()));

    // WHEN
    comp.ngOnInit();

    // THEN
    expect(profileService.getProfileInfo).toHaveBeenCalled();
  });

  it('should hold current authenticated user in variable account', () => {
    // WHEN
    comp.ngOnInit();

    // THEN
    expect(comp.account()).toBeNull();

    // WHEN
    accountService.authenticate(account);

    // THEN
    expect(comp.account()).toEqual(account);

    // WHEN
    accountService.authenticate(null);

    // THEN
    expect(comp.account()).toBeNull();
  });

  it('should hold current authenticated user in variable account if user is authenticated before page load', () => {
    // GIVEN
    accountService.authenticate(account);

    // WHEN
    comp.ngOnInit();

    // THEN
    expect(comp.account()).toEqual(account);

    // WHEN
    accountService.authenticate(null);

    // THEN
    expect(comp.account()).toBeNull();
  });

  it.each(['ROLE_ESTUDIANTE', 'ROLE_TUTOR', 'ROLE_JURADO', 'ROLE_USER', 'ROLE_ADMIN'])(
    'muestra Mis proyectos a %s y reserva Entidades para el admin',
    async authority => {
      vi.spyOn(profileService, 'getProfileInfo').mockReturnValue(of(new ProfileInfo()));
      accountService.authenticate({ ...account, authorities: [authority] });
      fixture.detectChanges();
      await fixture.whenStable();
      const root: HTMLElement = fixture.nativeElement;
      expect(root.querySelector('[data-cy="mis-proyectos"]')?.getAttribute('href')).toBe('/seguimiento');
      expect(root.querySelector('[data-cy="entity"]') !== null).toBe(authority === 'ROLE_ADMIN');
    },
  );

  it('oculta los enlaces de proyectos y entidades sin sesión', async () => {
    vi.spyOn(profileService, 'getProfileInfo').mockReturnValue(of(new ProfileInfo()));
    fixture.detectChanges();
    await fixture.whenStable();
    const root: HTMLElement = fixture.nativeElement;
    expect(root.querySelector('[data-cy="mis-proyectos"]')).toBeNull();
    expect(root.querySelector('[data-cy="entity"]')).toBeNull();
  });
});
