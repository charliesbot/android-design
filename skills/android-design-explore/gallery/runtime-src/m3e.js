// Entry of the proposal runtime: registers the @m3e/web elements proposals may use,
// then the runtime's own pieces. Built to ../public/runtime/m3e.js (see README.md).
import '@m3e/web/theme';
import '@m3e/web/app-bar';
import '@m3e/web/button';
import '@m3e/web/button-group';
import '@m3e/web/icon-button';
import '@m3e/web/split-button';
import '@m3e/web/fab';
import '@m3e/web/fab-menu';
import '@m3e/web/card';
import '@m3e/web/list';
import '@m3e/web/chips';
import '@m3e/web/toolbar';
import '@m3e/web/nav-bar';
import '@m3e/web/nav-rail';
import '@m3e/web/loading-indicator';
import '@m3e/web/progress-indicator';
import '@m3e/web/shape';
import '@m3e/web/switch';
import '@m3e/web/slider';
import '@m3e/web/form-field';

import './p-icon.js';
import './p-floating-toolbar.js';
import './theme-bridge.js';
import { runtimeChecks, runtimeSettle } from './checks.js';

window.runtimeChecks = runtimeChecks;
window.runtimeSettle = runtimeSettle;
