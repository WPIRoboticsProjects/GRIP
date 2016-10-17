/// <reference path="../typings/index.d.ts" />
export var ACTION;
(function (ACTION) {
    ACTION[ACTION["IncrementCounter"] = 0] = "IncrementCounter";
    ACTION[ACTION["DecrementCounter"] = 1] = "DecrementCounter";
    ACTION[ACTION["AddCounter"] = 2] = "AddCounter";
})(ACTION || (ACTION = {}));
export var OPERATION_ACTION;
(function (OPERATION_ACTION) {
    OPERATION_ACTION[OPERATION_ACTION["Add"] = 0] = "Add";
})(OPERATION_ACTION || (OPERATION_ACTION = {}));
export function createOperationStep(name) {
    return { type: OPERATION_ACTION.Add, name };
}
export function incrementCounter(counterId) {
    return { type: ACTION.IncrementCounter, counterId };
}
export function decrementCounter(counterId) {
    return { type: ACTION.DecrementCounter, counterId };
}
export function addCounter() {
    return { type: ACTION.AddCounter };
}
