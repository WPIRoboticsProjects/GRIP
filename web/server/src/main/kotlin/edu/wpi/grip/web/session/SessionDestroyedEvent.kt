package edu.wpi.grip.web.session

import javax.servlet.http.HttpSession


class SessionDestroyedEvent constructor(val session: HttpSession) {
}
