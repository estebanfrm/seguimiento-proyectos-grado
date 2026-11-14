import { beforeEach, describe, expect, it, vi } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { provideTranslateService } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { IProyectoGrado } from 'app/entities/proyecto-grado/proyecto-grado.model';
import { ProyectoGradoService } from 'app/entities/proyecto-grado/service/proyecto-grado.service';
import { IFechaLimiteEtapa } from '../fecha-limite-etapa.model';
import { FechaLimiteEtapaService } from '../service/fecha-limite-etapa.service';

import { FechaLimiteEtapaFormService } from './fecha-limite-etapa-form.service';
import { FechaLimiteEtapaUpdate } from './fecha-limite-etapa-update';

describe('FechaLimiteEtapa Management Update Component', () => {
  let comp: FechaLimiteEtapaUpdate;
  let fixture: ComponentFixture<FechaLimiteEtapaUpdate>;
  let activatedRoute: ActivatedRoute;
  let fechaLimiteEtapaFormService: FechaLimiteEtapaFormService;
  let fechaLimiteEtapaService: FechaLimiteEtapaService;
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

    fixture = TestBed.createComponent(FechaLimiteEtapaUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    fechaLimiteEtapaFormService = TestBed.inject(FechaLimiteEtapaFormService);
    fechaLimiteEtapaService = TestBed.inject(FechaLimiteEtapaService);
    proyectoGradoService = TestBed.inject(ProyectoGradoService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call ProyectoGrado query and add missing value', () => {
      const fechaLimiteEtapa: IFechaLimiteEtapa = { id: 20756 };
      const proyecto: IProyectoGrado = { id: 32010 };
      fechaLimiteEtapa.proyecto = proyecto;

      const proyectoGradoCollection: IProyectoGrado[] = [{ id: 32010 }];
      vi.spyOn(proyectoGradoService, 'query').mockReturnValue(of(new HttpResponse({ body: proyectoGradoCollection })));
      const additionalProyectoGrados = [proyecto];
      const expectedCollection: IProyectoGrado[] = [...additionalProyectoGrados, ...proyectoGradoCollection];
      vi.spyOn(proyectoGradoService, 'addProyectoGradoToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ fechaLimiteEtapa });
      comp.ngOnInit();

      expect(proyectoGradoService.query).toHaveBeenCalled();
      expect(proyectoGradoService.addProyectoGradoToCollectionIfMissing).toHaveBeenCalledWith(
        proyectoGradoCollection,
        ...additionalProyectoGrados.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.proyectoGradosSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const fechaLimiteEtapa: IFechaLimiteEtapa = { id: 20756 };
      const proyecto: IProyectoGrado = { id: 32010 };
      fechaLimiteEtapa.proyecto = proyecto;

      activatedRoute.data = of({ fechaLimiteEtapa });
      comp.ngOnInit();

      expect(comp.proyectoGradosSharedCollection()).toContainEqual(proyecto);
      expect(comp.fechaLimiteEtapa).toEqual(fechaLimiteEtapa);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<IFechaLimiteEtapa>();
      const fechaLimiteEtapa = { id: 31350 };
      vi.spyOn(fechaLimiteEtapaFormService, 'getFechaLimiteEtapa').mockReturnValue(fechaLimiteEtapa);
      vi.spyOn(fechaLimiteEtapaService, 'update').mockReturnValue(saveSubject);
      vi.spyOn(comp, 'previousState');
      activatedRoute.data = of({ fechaLimiteEtapa });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(fechaLimiteEtapa);
      saveSubject.complete();

      // THEN
      expect(fechaLimiteEtapaFormService.getFechaLimiteEtapa).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(fechaLimiteEtapaService.update).toHaveBeenCalledWith(expect.objectContaining(fechaLimiteEtapa));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<IFechaLimiteEtapa>();
      const fechaLimiteEtapa = { id: 31350 };
      vi.spyOn(fechaLimiteEtapaFormService, 'getFechaLimiteEtapa').mockReturnValue({ id: null });
      vi.spyOn(fechaLimiteEtapaService, 'create').mockReturnValue(saveSubject);
      vi.spyOn(comp, 'previousState');
      activatedRoute.data = of({ fechaLimiteEtapa: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(fechaLimiteEtapa);
      saveSubject.complete();

      // THEN
      expect(fechaLimiteEtapaFormService.getFechaLimiteEtapa).toHaveBeenCalled();
      expect(fechaLimiteEtapaService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<IFechaLimiteEtapa>();
      const fechaLimiteEtapa = { id: 31350 };
      vi.spyOn(fechaLimiteEtapaService, 'update').mockReturnValue(saveSubject);
      vi.spyOn(comp, 'previousState');
      activatedRoute.data = of({ fechaLimiteEtapa });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(fechaLimiteEtapaService.update).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
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
