export * from './auth.service';
import { AuthService } from './auth.service';
export * from './cart.service';
import { CartService } from './cart.service';
export * from './orders.service';
import { OrdersService } from './orders.service';
export * from './products.service';
import { ProductsService } from './products.service';
export const APIS = [AuthService, CartService, OrdersService, ProductsService];
