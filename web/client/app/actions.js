/// <reference path="../typings/index.d.ts" />
export var ACTION;
(function (ACTION) {
    ACTION[ACTION["IncrementCounter"] = 0] = "IncrementCounter";
    ACTION[ACTION["DecrementCounter"] = 1] = "DecrementCounter";
    ACTION[ACTION["AddCounter"] = 2] = "AddCounter";
})(ACTION || (ACTION = {}));
export function incrementCounter(counterId) {
    return { type: ACTION.IncrementCounter, counterId };
}
export function decrementCounter(counterId) {
    return { type: ACTION.DecrementCounter, counterId };
}
export function addCounter() {
    return { type: ACTION.AddCounter };
}
