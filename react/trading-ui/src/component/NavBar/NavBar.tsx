import { NavLink } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faAddressBook as dashboardIcon,
  faChartLine as quotesIcon,
  faCircle as headerIcon,
} from "@fortawesome/free-solid-svg-icons";
import "./NavBar.scss";

export default function NavBar() {
  const navItemClass = ({ isActive }: { isActive: boolean }) =>
    `page-navigation-item${isActive ? " active" : ""}`;

  return (
    <nav className="page-navigation">
      <NavLink to="/dashboard" className="page-navigation-header" aria-label="Home">
        <FontAwesomeIcon icon={headerIcon} />
      </NavLink>

      <NavLink to="/dashboard" className={navItemClass} aria-label="Dashboard">
        <FontAwesomeIcon icon={dashboardIcon} />
      </NavLink>

      <NavLink to="/quotes" className={navItemClass} aria-label="Quotes">
        <FontAwesomeIcon icon={quotesIcon} />
      </NavLink>
    </nav>
  );
}

