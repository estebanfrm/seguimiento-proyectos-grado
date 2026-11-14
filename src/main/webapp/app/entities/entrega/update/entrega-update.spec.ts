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
import { IEntrega } from '../entrega.model';
import { EntregaService } from '../service/entrega.service';

import { EntregaFormService } from './entrega-form.service';
import { EntregaUpdate } from './entrega-update';

describe('Entrega Management Update Component', () => {
  let comp: EntregaUpdate;
  let fixture: ComponentFixture<EntregaUpdate>;
  let activatedRoute: ActivatedRoute;
  let entregaFormService: EntregaFormService;
  let entregaService: EntregaService;
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

    fixture = TestBed.createComponent(EntregaUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    entregaFormService = TestBed.inject(EntregaFormService);
    entregaService = TestBed.inject(EntregaService);
    userService = TestBed.inject(UserService);
    proyectoGradoService = TestBed.inject(ProyectoGradoService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call User query and add missing value', () => {
      const entrega: IEntrega = { id: 5892 };
      const autor: IUser = { id: 3944 };
      entrega.autor = autor;

      const userCollection: IUser[] = [{ id: 3944 }];
      vi.spyOn(userService, 'query').mockReturnValue(of(new HttpResponse({ body: userCollection })));
      const additionalUsers = [autor];
      const expectedCollection: IUser[] = [...additionalUsers, ...userCollection];
      vi.spyOn(userService, 'addUserToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ entrega });
      comp.ngOnInit();

      expect(userService.query).toHaveBeenCalled();
      expect(userService.addUserToCollectionIfMissing).toHaveBeenCalledWith(
        userCollection,
        ...additionalUsers.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.usersSharedCollection()).toEqual(expectedCollection);
    });

    it('should call ProyectoGrado query and add missing value', () => {
      const entrega: IEntrega = { id: 5892 };
      const proyecto: IProyectoGrado = { id: 32010 };
      entrega.proyecto = proyecto;

      const proyectoGradoCollection: IProyectoGrado[] = [{ id: 32010 }];
      vi.spyOn(proyectoGradoService, 'query').mockReturnValue(of(new HttpResponse({ body: proyectoGradoCollection })));
      const additionalProyectoGrados = [proyecto];
      const expectedCollection: IProyectoGrado[] = [...additionalProyectoGrados, ...proyectoGradoCollection];
      vi.spyOn(proyectoGradoService, 'addProyectoGradoToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ entrega });
      comp.ngOnInit();

      expect(proyectoGradoService.query).toHaveBeenCalled();
      expect(proyectoGradoService.addProyectoGradoToCollectionIfMissing).toHaveBeenCalledWith(
        proyectoGradoCollection,
        ...additionalProyectoGrados.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.proyectoGradosSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const entrega: IEntrega = { id: 5892 };
      const autor: IUser = { id: 3944 };
      entrega.autor = autor;
      const proyecto: IProyectoGrado = { id: 32010 };
      entrega.proyecto = proyecto;

      activatedRoute.data = of({ entrega });
      comp.ngOnInit();

      expect(comp.usersSharedCollection()).toContainEqual(autor);
      expect(comp.proyectoGradosSharedCollection()).toContainEqual(proyecto);
      expect(comp.entrega).toEqual(entrega);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<IEntrega>();
      const entrega = { id: 14050 };
      vi.spyOn(entregaFormService, 'getEntrega').mockReturnValue(entrega);
      vi.spyOn(entregaService, 'update').mockReturnValue(saveSubject);
      vi.spyOn(comp, 'previousState');
      activatedRoute.data = of({ entrega });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(entrega);
      saveSubject.complete();

      // THEN
      expect(entregaFormService.getEntrega).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(entregaService.update).toHaveBeenCalledWith(expect.objectContaining(entrega));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<IEntrega>();
      const entrega = { id: 14050 };
      vi.spyOn(entregaFormService, 'getEntrega').mockReturnValue({ id: null });
      vi.spyOn(entregaService, 'create').mockReturnValue(saveSubject);
      vi.spyOn(comp, 'previousState');
      activatedRoute.data = of({ entrega: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(entrega);
      saveSubject.complete();

      // THEN
      expect(entregaFormService.getEntrega).toHaveBeenCalled();
      expect(entregaService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<IEntrega>();
      const entrega = { id: 14050 };
      vi.spyOn(entregaService, 'update').mockReturnValue(saveSubject);
      vi.spyOn(comp, 'previousState');
      activatedRoute.data = of({ entrega });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(entregaService.update).toHaveBeenCalled();
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
