import { beforeEach, describe, expect, it, vi } from 'vitest';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { FaIconLibrary } from '@fortawesome/angular-fontawesome';
import { faBan, faSave } from '@fortawesome/free-solid-svg-icons';
import { TranslateService, provideTranslateService } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import userManagementTranslations from '../../../../../i18n/es/user-management.json';
import { UserManagementService } from '../service/user-management.service';
import { IUserManagement } from '../user-management.model';

import { UserManagementFormService } from './user-management-form.service';
import { UserManagementUpdate } from './user-management-update';

describe('UserManagement Management Update Component', () => {
  let comp: UserManagementUpdate;
  let fixture: ComponentFixture<UserManagementUpdate>;
  let activatedRoute: ActivatedRoute;
  let userManagementFormService: UserManagementFormService;
  let userManagementService: UserManagementService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideTranslateService(),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    });

    fixture = TestBed.createComponent(UserManagementUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    userManagementFormService = TestBed.inject(UserManagementFormService);
    userManagementService = TestBed.inject(UserManagementService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should update editForm', () => {
      const userManagement: IUserManagement = { login: 'Ana.AlbaRubio33' };

      activatedRoute.data = of({ userManagement });
      comp.ngOnInit();

      expect(comp.userManagement).toEqual(userManagement);
    });
  });

  describe('save', () => {
    it('should display readable roles and submit their authority codes when creating a tutor', () => {
      const translate = TestBed.inject(TranslateService);
      translate.setTranslation('es', userManagementTranslations);
      translate.use('es');
      TestBed.inject(FaIconLibrary).addIcons(faBan, faSave);
      activatedRoute.data = of({ userManagement: null });
      vi.spyOn(comp, 'authorities').mockReturnValue(['ROLE_ADMIN', 'ROLE_USER', 'ROLE_ESTUDIANTE', 'ROLE_TUTOR', 'ROLE_JURADO']);
      vi.spyOn(userManagementService, 'create').mockReturnValue(of({ login: 'tutor3' }));
      vi.spyOn(comp, 'previousState');
      fixture.detectChanges();
      comp.editForm.patchValue({ login: 'tutor3', email: 'tutor3@sgpg.example' });

      const select = (fixture.nativeElement as HTMLElement).querySelector<HTMLSelectElement>('[data-cy="authorities"]')!;
      expect(Array.from(select.options, option => option.textContent)).toEqual(['Coordinador', 'Usuario', 'Estudiante', 'Tutor', 'Jurado']);
      comp.editForm.patchValue({ authorities: ['ROLE_USER', 'ROLE_TUTOR'] });
      comp.save();

      expect(userManagementService.create).toHaveBeenCalledWith(
        expect.objectContaining({ login: 'tutor3', authorities: ['ROLE_USER', 'ROLE_TUTOR'] }),
      );
    });

    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<IUserManagement>();
      const userManagement = { login: 'Marisol.AlvaIglesias' };
      vi.spyOn(userManagementFormService, 'getUserManagement').mockReturnValue(userManagement);
      vi.spyOn(userManagementService, 'update').mockReturnValue(saveSubject);
      vi.spyOn(comp, 'previousState');
      activatedRoute.data = of({ userManagement });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(userManagement);
      saveSubject.complete();

      // THEN
      expect(userManagementFormService.getUserManagement).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(userManagementService.update).toHaveBeenCalledWith(expect.objectContaining(userManagement));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<IUserManagement>();
      const userManagement = { login: 'Marisol.AlvaIglesias' };
      vi.spyOn(userManagementFormService, 'getUserManagement').mockReturnValue({ id: null, login: null });
      vi.spyOn(userManagementService, 'create').mockReturnValue(saveSubject);
      vi.spyOn(comp, 'previousState');
      activatedRoute.data = of({ userManagement: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(userManagement);
      saveSubject.complete();

      // THEN
      expect(userManagementFormService.getUserManagement).toHaveBeenCalled();
      expect(userManagementService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<IUserManagement>();
      const userManagement = { id: 123, login: 'Marisol.AlvaIglesias' };
      vi.spyOn(userManagementService, 'update').mockReturnValue(saveSubject);
      vi.spyOn(comp, 'previousState');
      activatedRoute.data = of({ userManagement });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(userManagementService.update).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
