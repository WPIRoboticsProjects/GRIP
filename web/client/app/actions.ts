/// <reference path="../typings/index.d.ts" />

export enum ACTION { IncrementCounter, DecrementCounter, AddCounter }

export interface ICounterAction {
  type: ACTION;
  counterId?: number;
}

export enum OPERATION_ACTION { Add, }

export interface IOperationAction {
  type: OPERATION_ACTION;
  name: string;
}

export function createOperationStep(name: string): IOperationAction {
  return {type: OPERATION_ACTION.Add, name};
}

export function incrementCounter(counterId: number): ICounterAction {
  return {type: ACTION.IncrementCounter, counterId};
}

export function decrementCounter(counterId: number): ICounterAction {
  return {type: ACTION.DecrementCounter, counterId};
}

export function addCounter(): ICounterAction {
  return {type: ACTION.AddCounter};
}
