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
import { IHistorialEtapa } from '../historial-etapa.model';
import { HistorialEtapaService } from '../service/historial-etapa.service';

import { HistorialEtapaFormService } from './historial-etapa-form.service';
import { HistorialEtapaUpdate } from './historial-etapa-update';

describe('HistorialEtapa Management Update Component', () => {
  let comp: HistorialEtapaUpdate;
  let fixture: ComponentFixture<HistorialEtapaUpdate>;
  let activatedRoute: ActivatedRoute;
  let historialEtapaFormService: HistorialEtapaFormService;
  let historialEtapaService: HistorialEtapaService;
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

    fixture = TestBed.createComponent(HistorialEtapaUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    historialEtapaFormService = TestBed.inject(HistorialEtapaFormService);
    historialEtapaService = TestBed.inject(HistorialEtapaService);
    userService = TestBed.inject(UserService);
    proyectoGradoService = TestBed.inject(ProyectoGradoService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call User query and add missing value', () => {
      const historialEtapa: IHistorialEtapa = { id: 21158 };
      const usuario: IUser = { id: 3944 };
      historialEtapa.usuario = usuario;

      const userCollection: IUser[] = [{ id: 3944 }];
      vi.spyOn(userService, 'query').mockReturnValue(of(new HttpResponse({ body: userCollection })));
      const additionalUsers = [usuario];
      const expectedCollection: IUser[] = [...additionalUsers, ...userCollection];
      vi.spyOn(userService, 'addUserToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ historialEtapa });
      comp.ngOnInit();

      expect(userService.query).toHaveBeenCalled();
      expect(userService.addUserToCollectionIfMissing).toHaveBeenCalledWith(
        userCollection,
        ...additionalUsers.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.usersSharedCollection()).toEqual(expectedCollection);
    });

    it('should call ProyectoGrado query and add missing value', () => {
      const historialEtapa: IHistorialEtapa = { id: 21158 };
      const proyecto: IProyectoGrado = { id: 32010 };
      historialEtapa.proyecto = proyecto;

      const proyectoGradoCollection: IProyectoGrado[] = [{ id: 32010 }];
      vi.spyOn(proyectoGradoService, 'query').mockReturnValue(of(new HttpResponse({ body: proyectoGradoCollection })));
      const additionalProyectoGrados = [proyecto];
      const expectedCollection: IProyectoGrado[] = [...additionalProyectoGrados, ...proyectoGradoCollection];
      vi.spyOn(proyectoGradoService, 'addProyectoGradoToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ historialEtapa });
      comp.ngOnInit();

      expect(proyectoGradoService.query).toHaveBeenCalled();
      expect(proyectoGradoService.addProyectoGradoToCollectionIfMissing).toHaveBeenCalledWith(
        proyectoGradoCollection,
        ...additionalProyectoGrados.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.proyectoGradosSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const historialEtapa: IHistorialEtapa = { id: 21158 };
      const usuario: IUser = { id: 3944 };
      historialEtapa.usuario = usuario;
      const proyecto: IProyectoGrado = { id: 32010 };
      historialEtapa.proyecto = proyecto;

      activatedRoute.data = of({ historialEtapa });
      comp.ngOnInit();

      expect(comp.usersSharedCollection()).toContainEqual(usuario);
      expect(comp.proyectoGradosSharedCollection()).toContainEqual(proyecto);
      expect(comp.historialEtapa).toEqual(historialEtapa);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<IHistorialEtapa>();
      const historialEtapa = { id: 2724 };
      vi.spyOn(historialEtapaFormService, 'getHistorialEtapa').mockReturnValue(historialEtapa);
      vi.spyOn(historialEtapaService, 'update').mockReturnValue(saveSubject);
      vi.spyOn(comp, 'previousState');
      activatedRoute.data = of({ historialEtapa });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(historialEtapa);
      saveSubject.complete();

      // THEN
      expect(historialEtapaFormService.getHistorialEtapa).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(historialEtapaService.update).toHaveBeenCalledWith(expect.objectContaining(historialEtapa));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<IHistorialEtapa>();
      const historialEtapa = { id: 2724 };
      vi.spyOn(historialEtapaFormService, 'getHistorialEtapa').mockReturnValue({ id: null });
      vi.spyOn(historialEtapaService, 'create').mockReturnValue(saveSubject);
      vi.spyOn(comp, 'previousState');
      activatedRoute.data = of({ historialEtapa: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(historialEtapa);
      saveSubject.complete();

      // THEN
      expect(historialEtapaFormService.getHistorialEtapa).toHaveBeenCalled();
      expect(historialEtapaService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<IHistorialEtapa>();
      const historialEtapa = { id: 2724 };
      vi.spyOn(historialEtapaService, 'update').mockReturnValue(saveSubject);
      vi.spyOn(comp, 'previousState');
      activatedRoute.data = of({ historialEtapa });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(historialEtapaService.update).toHaveBeenCalled();
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
