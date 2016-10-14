/// <reference path="../typings/index.d.ts" />

import {Reducer, combineReducers} from 'redux';
import {ICounterAction, ACTION} from './actions';

function counters(state: number[] = [0, 0, 0], action: ICounterAction): number[] {
  switch (action.type) {
    case ACTION.IncrementCounter:
      return [
        ...state.slice(0, action.counterId),
        state[action.counterId] + 1,
        ...state.slice(action.counterId + 1),
      ];

    case ACTION.DecrementCounter:
      return [
        ...state.slice(0, action.counterId),
        state[action.counterId] - 1,
        ...state.slice(action.counterId + 1),
      ];

    case ACTION.AddCounter:
      return [...state, 0];

    default:
      return state;
  }
}

export const counterApp: Reducer = combineReducers({counters});
