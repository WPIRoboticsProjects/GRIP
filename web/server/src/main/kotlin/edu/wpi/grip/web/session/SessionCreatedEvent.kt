package edu.wpi.grip.web.session

import javax.servlet.http.HttpSession


class SessionCreatedEvent constructor(val session: HttpSession) {
}
