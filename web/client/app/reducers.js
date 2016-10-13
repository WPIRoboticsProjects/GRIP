/// <reference path="../typings/index.d.ts" />
import { combineReducers } from 'redux';
import { ACTION } from './actions';
function counters(state = [0, 0, 0], action) {
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
export const counterApp = combineReducers({ counters });
