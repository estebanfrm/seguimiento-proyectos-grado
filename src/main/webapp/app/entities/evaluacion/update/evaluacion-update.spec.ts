import { beforeEach, describe, expect, it, vi } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { provideTranslateService } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { IProyectoGrado } from 'app/entities/proyecto-grado/proyecto-grado.model';
import { ProyectoGradoService } from 'app/entities/proyecto-grado/service/proyecto-grado.service';
import { UserService } from 'app/entities/user/service/user.service';
import { IUser } from 'app/entities/user/user.model';
import { IEvaluacion } from '../evaluacion.model';
import { EvaluacionService } from '../service/evaluacion.service';

import { EvaluacionFormService } from './evaluacion-form.service';
import { EvaluacionUpdate } from './evaluacion-update';

describe('Evaluacion Management Update Component', () => {
  let comp: EvaluacionUpdate;
  let fixture: ComponentFixture<EvaluacionUpdate>;
  let activatedRoute: ActivatedRoute;
  let evaluacionFormService: EvaluacionFormService;
  let evaluacionService: EvaluacionService;
  let userService: UserService;
  let proyectoGradoService: ProyectoGradoService;

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

    fixture = TestBed.createComponent(EvaluacionUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    evaluacionFormService = TestBed.inject(EvaluacionFormService);
    evaluacionService = TestBed.inject(EvaluacionService);
    userService = TestBed.inject(UserService);
    proyectoGradoService = TestBed.inject(ProyectoGradoService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call User query and add missing value', () => {
      const evaluacion: IEvaluacion = { id: 1988 };
      const jurado: IUser = { id: 3944 };
      evaluacion.jurado = jurado;

      const userCollection: IUser[] = [{ id: 3944 }];
      vi.spyOn(userService, 'query').mockReturnValue(of(new HttpResponse({ body: userCollection })));
      const additionalUsers = [jurado];
      const expectedCollection: IUser[] = [...additionalUsers, ...userCollection];
      vi.spyOn(userService, 'addUserToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ evaluacion });
      comp.ngOnInit();

      expect(userService.query).toHaveBeenCalled();
      expect(userService.addUserToCollectionIfMissing).toHaveBeenCalledWith(
        userCollection,
        ...additionalUsers.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.usersSharedCollection()).toEqual(expectedCollection);
    });

    it('should call ProyectoGrado query and add missing value', () => {
      const evaluacion: IEvaluacion = { id: 1988 };
      const proyecto: IProyectoGrado = { id: 32010 };
      evaluacion.proyecto = proyecto;

      const proyectoGradoCollection: IProyectoGrado[] = [{ id: 32010 }];
      vi.spyOn(proyectoGradoService, 'query').mockReturnValue(of(new HttpResponse({ body: proyectoGradoCollection })));
      const additionalProyectoGrados = [proyecto];
      const expectedCollection: IProyectoGrado[] = [...additionalProyectoGrados, ...proyectoGradoCollection];
      vi.spyOn(proyectoGradoService, 'addProyectoGradoToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ evaluacion });
      comp.ngOnInit();

      expect(proyectoGradoService.query).toHaveBeenCalled();
      expect(proyectoGradoService.addProyectoGradoToCollectionIfMissing).toHaveBeenCalledWith(
        proyectoGradoCollection,
        ...additionalProyectoGrados.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.proyectoGradosSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const evaluacion: IEvaluacion = { id: 1988 };
      const jurado: IUser = { id: 3944 };
      evaluacion.jurado = jurado;
      const proyecto: IProyectoGrado = { id: 32010 };
      evaluacion.proyecto = proyecto;

      activatedRoute.data = of({ evaluacion });
      comp.ngOnInit();

      expect(comp.usersSharedCollection()).toContainEqual(jurado);
      expect(comp.proyectoGradosSharedCollection()).toContainEqual(proyecto);
      expect(comp.evaluacion).toEqual(evaluacion);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<IEvaluacion>();
      const evaluacion = { id: 10220 };
      vi.spyOn(evaluacionFormService, 'getEvaluacion').mockReturnValue(evaluacion);
      vi.spyOn(evaluacionService, 'update').mockReturnValue(saveSubject);
      vi.spyOn(comp, 'previousState');
      activatedRoute.data = of({ evaluacion });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(evaluacion);
      saveSubject.complete();

      // THEN
      expect(evaluacionFormService.getEvaluacion).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(evaluacionService.update).toHaveBeenCalledWith(expect.objectContaining(evaluacion));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<IEvaluacion>();
      const evaluacion = { id: 10220 };
      vi.spyOn(evaluacionFormService, 'getEvaluacion').mockReturnValue({ id: null });
      vi.spyOn(evaluacionService, 'create').mockReturnValue(saveSubject);
      vi.spyOn(comp, 'previousState');
      activatedRoute.data = of({ evaluacion: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(evaluacion);
      saveSubject.complete();

      // THEN
      expect(evaluacionFormService.getEvaluacion).toHaveBeenCalled();
      expect(evaluacionService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<IEvaluacion>();
      const evaluacion = { id: 10220 };
      vi.spyOn(evaluacionService, 'update').mockReturnValue(saveSubject);
      vi.spyOn(comp, 'previousState');
      activatedRoute.data = of({ evaluacion });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(evaluacionService.update).toHaveBeenCalled();
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

    describe('compareProyectoGrado', () => {
      it('should forward to proyectoGradoService', () => {
        const entity = { id: 32010 };
        const entity2 = { id: 28782 };
        vi.spyOn(proyectoGradoService, 'compareProyectoGrado');
        comp.compareProyectoGrado(entity, entity2);
        expect(proyectoGradoService.compareProyectoGrado).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
