import type { GlobalThemeOverrides } from "naive-ui"

const themeOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: "#18A058FF",
    primaryColorHover: "#36AD6AFF",
    primaryColorPressed: "#0C7A43FF",
    primaryColorSuppl: "#36AD6AFF",
    infoColor: "#409EFFFF",
    infoColorHover: "#66B1FFFF",
    infoColorPressed: "#3A8EE6FF",
    infoColorSuppl: "rgba(64, 158, 255, 1)",
    successColor: "#18A058FF",
    successColorHover: "#36AD6AFF",
    successColorPressed: "#0C7A43FF",
    successColorSuppl: "#36AD6AFF",
    warningColor: "#E6A23CFF",
    warningColorHover: "#EBB563FF",
    warningColorPressed: "#CF9236FF",
    warningColorSuppl: "rgba(230, 162, 60, 1)",
    errorColor: "#F56C6CFF",
    errorColorHover: "#F78989FF",
    errorColorPressed: "#DD6161FF",
    errorColorSuppl: "rgba(245, 108, 108, 1)"
  },
  Switch: {
    railColorActive: "#18A058FF",
    loadingColor: "#18A058FF"
  }
}

const themeOverridesDark: GlobalThemeOverrides = {
  common: {
    bodyColor: "#0D1117FF",
    borderColor: "#21262DFF"
  },
  Layout: {
    headerColor: "#161B22FF",
    headerBorderColor: "#161B22FF",
    siderColor: "#161B22FF",
    siderBorderColor: "#0D1117FF",
    color: "#0D1117FF"
  },
  Divider: {
    color: "#161B22FF"
  },
  Button: {
    colorTertiary: "#161B22FF"
  },
  Switch: {
    railColorActive: "#18A058E0",
    loadingColor: "#18A058E0"
  },
  Input: {
    color: "#161B22FF"
  },
  Card: {
    color: "#00000000",
    colorModal: "#161B22FF",
    borderColor: "#161b22FF"
  },
  Tabs: {
    tabBorderColor: "#161B22FF"
  },
  Drawer: {
    color: "#0D1117FF"
  },
  Dropdown: {
    color: "#222D3DFF"
  },
  Table: {
    borderColor: "#161B22FF",
    borderColorModal: "#26292FFF",
    thColor: "#161B22FF",
    tdColor: "#00000000",
    tdColorHover: "#161B22FF",
    tdColorStriped: "#161B22FF",
    thColorModal: "#26292FFF",
    tdColorModal: "#00000000"
  },
  DataTable: {
    borderColor: "#161B22FF",
    thColor: "#161B22FF",
    tdColor: "#00000000",
    tdColorHover: "#161B22FF",
    tdColorStriped: "#161B22FF"
  },
  DatePicker: {
    panelColor: "#222D3DFF"
  },
  Popover: {
    color: "#222D3DFF"
  },
  Transfer: {
    listColor: "#161B22FF"
  },
  Message: {
    color: "#222D3DFF",
    colorInfo: "#222D3DFF",
    colorSuccess: "#222D3DFF",
    colorWarning: "#222D3DFF",
    colorError: "#222D3DFF",
    colorLoading: "#222D3DFF"
  },
  Notification: {
    color: "#222D3DFF"
  }
}

export { themeOverrides, themeOverridesDark }
