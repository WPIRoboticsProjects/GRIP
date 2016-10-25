/// <reference path="../typings/index.d.ts" />

import {OperationDescription, DefaultApi} from 'grip-swagger';
export enum ACTION {
  IncrementCounter = 1,
  DecrementCounter = 2,
  AddCounter = 3
}

export enum FETCH_OPERATIONS {
  Request = 4,
  Failure = 5,
  Success = 6
}

export enum OPERATION_ACTION {
  Add = 7
}

const api: DefaultApi = new DefaultApi(undefined, 'http://localhost:3000/api');

export interface ICounterAction {
  type: ACTION;
  counterId?: number;
}

export interface IOperationAction {
  type: OPERATION_ACTION;
  name: string;
}

export function createOperationStep(name: string): IOperationAction {
  return {type: OPERATION_ACTION.Add, name};
}

export interface IFetchOperationAction {
  type: FETCH_OPERATIONS;
  error?: string;
  response?: OperationDescription[];
}

function requestOperations(): IFetchOperationAction {
  return {type: FETCH_OPERATIONS.Request};
}

function receiveOperations(operations: OperationDescription[]): IFetchOperationAction {
  return {response: operations, type: FETCH_OPERATIONS.Success};
}

export function fetchOperations(): (dispatch: (event: any) => void) => Promise<any> {
  return function (dispatch: (event: any) => void): Promise<any> {
    dispatch(requestOperations());
    return api
      .operationsGet()
      .then((operations: OperationDescription[]) => {
        dispatch(receiveOperations(operations));
      }).catch(console.error);
  };
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
