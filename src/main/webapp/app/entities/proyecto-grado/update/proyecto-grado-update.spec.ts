import { beforeEach, describe, expect, it, vi } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { provideTranslateService } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { UserService } from 'app/entities/user/service/user.service';
import { IUser } from 'app/entities/user/user.model';
import { IProyectoGrado } from '../proyecto-grado.model';
import { ProyectoGradoService } from '../service/proyecto-grado.service';

import { ProyectoGradoFormService } from './proyecto-grado-form.service';
import { ProyectoGradoUpdate } from './proyecto-grado-update';

describe('ProyectoGrado Management Update Component', () => {
  let comp: ProyectoGradoUpdate;
  let fixture: ComponentFixture<ProyectoGradoUpdate>;
  let activatedRoute: ActivatedRoute;
  let proyectoGradoFormService: ProyectoGradoFormService;
  let proyectoGradoService: ProyectoGradoService;
  let userService: UserService;

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

    fixture = TestBed.createComponent(ProyectoGradoUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    proyectoGradoFormService = TestBed.inject(ProyectoGradoFormService);
    proyectoGradoService = TestBed.inject(ProyectoGradoService);
    userService = TestBed.inject(UserService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call User query and add missing value', () => {
      const proyectoGrado: IProyectoGrado = { id: 28782 };
      const tutor: IUser = { id: 3944 };
      proyectoGrado.tutor = tutor;
      const estudiantes: IUser[] = [{ id: 3944 }];
      proyectoGrado.estudiantes = estudiantes;
      const jurados: IUser[] = [{ id: 3944 }];
      proyectoGrado.jurados = jurados;

      const userCollection: IUser[] = [{ id: 3944 }];
      vi.spyOn(userService, 'query').mockReturnValue(of(new HttpResponse({ body: userCollection })));
      const additionalUsers = [tutor, ...estudiantes, ...jurados];
      const expectedCollection: IUser[] = [...additionalUsers, ...userCollection];
      vi.spyOn(userService, 'addUserToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ proyectoGrado });
      comp.ngOnInit();

      expect(userService.query).toHaveBeenCalled();
      expect(userService.addUserToCollectionIfMissing).toHaveBeenCalledWith(
        userCollection,
        ...additionalUsers.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.usersSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const proyectoGrado: IProyectoGrado = { id: 28782 };
      const tutor: IUser = { id: 3944 };
      proyectoGrado.tutor = tutor;
      const estudiante: IUser = { id: 3944 };
      proyectoGrado.estudiantes = [estudiante];
      const jurado: IUser = { id: 3944 };
      proyectoGrado.jurados = [jurado];

      activatedRoute.data = of({ proyectoGrado });
      comp.ngOnInit();

      expect(comp.usersSharedCollection()).toContainEqual(tutor);
      expect(comp.usersSharedCollection()).toContainEqual(estudiante);
      expect(comp.usersSharedCollection()).toContainEqual(jurado);
      expect(comp.proyectoGrado).toEqual(proyectoGrado);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<IProyectoGrado>();
      const proyectoGrado = { id: 32010 };
      vi.spyOn(proyectoGradoFormService, 'getProyectoGrado').mockReturnValue(proyectoGrado);
      vi.spyOn(proyectoGradoService, 'update').mockReturnValue(saveSubject);
      vi.spyOn(comp, 'previousState');
      activatedRoute.data = of({ proyectoGrado });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(proyectoGrado);
      saveSubject.complete();

      // THEN
      expect(proyectoGradoFormService.getProyectoGrado).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(proyectoGradoService.update).toHaveBeenCalledWith(expect.objectContaining(proyectoGrado));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<IProyectoGrado>();
      const proyectoGrado = { id: 32010 };
      vi.spyOn(proyectoGradoFormService, 'getProyectoGrado').mockReturnValue({ id: null });
      vi.spyOn(proyectoGradoService, 'create').mockReturnValue(saveSubject);
      vi.spyOn(comp, 'previousState');
      activatedRoute.data = of({ proyectoGrado: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(proyectoGrado);
      saveSubject.complete();

      // THEN
      expect(proyectoGradoFormService.getProyectoGrado).toHaveBeenCalled();
      expect(proyectoGradoService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<IProyectoGrado>();
      const proyectoGrado = { id: 32010 };
      vi.spyOn(proyectoGradoService, 'update').mockReturnValue(saveSubject);
      vi.spyOn(comp, 'previousState');
      activatedRoute.data = of({ proyectoGrado });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(proyectoGradoService.update).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareUser', () => {
      it('should forward to userService', () => {
        const entity = { id: 3944 };
        const entity2 = { id: 6275 };
        vi.spyOn(userService, 'compareUser');
        comp.compareUser(entity, entity2);
        expect(userService.compareUser).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
