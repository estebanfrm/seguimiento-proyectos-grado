import { beforeEach, describe, expect, it, vi } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { provideTranslateService } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { IEntrega } from 'app/entities/entrega/entrega.model';
import { EntregaService } from 'app/entities/entrega/service/entrega.service';
import { UserService } from 'app/entities/user/service/user.service';
import { IUser } from 'app/entities/user/user.model';
import { IObservacion } from '../observacion.model';
import { ObservacionService } from '../service/observacion.service';

import { ObservacionFormService } from './observacion-form.service';
import { ObservacionUpdate } from './observacion-update';

describe('Observacion Management Update Component', () => {
  let comp: ObservacionUpdate;
  let fixture: ComponentFixture<ObservacionUpdate>;
  let activatedRoute: ActivatedRoute;
  let observacionFormService: ObservacionFormService;
  let observacionService: ObservacionService;
  let userService: UserService;
  let entregaService: EntregaService;

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

    fixture = TestBed.createComponent(ObservacionUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    observacionFormService = TestBed.inject(ObservacionFormService);
    observacionService = TestBed.inject(ObservacionService);
    userService = TestBed.inject(UserService);
    entregaService = TestBed.inject(EntregaService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call User query and add missing value', () => {
      const observacion: IObservacion = { id: 6876 };
      const autor: IUser = { id: 3944 };
      observacion.autor = autor;

      const userCollection: IUser[] = [{ id: 3944 }];
      vi.spyOn(userService, 'query').mockReturnValue(of(new HttpResponse({ body: userCollection })));
      const additionalUsers = [autor];
      const expectedCollection: IUser[] = [...additionalUsers, ...userCollection];
      vi.spyOn(userService, 'addUserToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ observacion });
      comp.ngOnInit();

      expect(userService.query).toHaveBeenCalled();
      expect(userService.addUserToCollectionIfMissing).toHaveBeenCalledWith(
        userCollection,
        ...additionalUsers.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.usersSharedCollection()).toEqual(expectedCollection);
    });

    it('should call Entrega query and add missing value', () => {
      const observacion: IObservacion = { id: 6876 };
      const entrega: IEntrega = { id: 14050 };
      observacion.entrega = entrega;

      const entregaCollection: IEntrega[] = [{ id: 14050 }];
      vi.spyOn(entregaService, 'query').mockReturnValue(of(new HttpResponse({ body: entregaCollection })));
      const additionalEntregas = [entrega];
      const expectedCollection: IEntrega[] = [...additionalEntregas, ...entregaCollection];
      vi.spyOn(entregaService, 'addEntregaToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ observacion });
      comp.ngOnInit();

      expect(entregaService.query).toHaveBeenCalled();
      expect(entregaService.addEntregaToCollectionIfMissing).toHaveBeenCalledWith(
        entregaCollection,
        ...additionalEntregas.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.entregasSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const observacion: IObservacion = { id: 6876 };
      const autor: IUser = { id: 3944 };
      observacion.autor = autor;
      const entrega: IEntrega = { id: 14050 };
      observacion.entrega = entrega;

      activatedRoute.data = of({ observacion });
      comp.ngOnInit();

      expect(comp.usersSharedCollection()).toContainEqual(autor);
      expect(comp.entregasSharedCollection()).toContainEqual(entrega);
      expect(comp.observacion).toEqual(observacion);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<IObservacion>();
      const observacion = { id: 9028 };
      vi.spyOn(observacionFormService, 'getObservacion').mockReturnValue(observacion);
      vi.spyOn(observacionService, 'update').mockReturnValue(saveSubject);
      vi.spyOn(comp, 'previousState');
      activatedRoute.data = of({ observacion });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(observacion);
      saveSubject.complete();

      // THEN
      expect(observacionFormService.getObservacion).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(observacionService.update).toHaveBeenCalledWith(expect.objectContaining(observacion));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<IObservacion>();
      const observacion = { id: 9028 };
      vi.spyOn(observacionFormService, 'getObservacion').mockReturnValue({ id: null });
      vi.spyOn(observacionService, 'create').mockReturnValue(saveSubject);
      vi.spyOn(comp, 'previousState');
      activatedRoute.data = of({ observacion: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(observacion);
      saveSubject.complete();

      // THEN
      expect(observacionFormService.getObservacion).toHaveBeenCalled();
      expect(observacionService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<IObservacion>();
      const observacion = { id: 9028 };
      vi.spyOn(observacionService, 'update').mockReturnValue(saveSubject);
      vi.spyOn(comp, 'previousState');
      activatedRoute.data = of({ observacion });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(observacionService.update).toHaveBeenCalled();
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

    describe('compareEntrega', () => {
      it('should forward to entregaService', () => {
        const entity = { id: 14050 };
        const entity2 = { id: 5892 };
        vi.spyOn(entregaService, 'compareEntrega');
        comp.compareEntrega(entity, entity2);
        expect(entregaService.compareEntrega).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
