import { Injectable, signal } from '@angular/core';

export const TRANSLATIONS: Record<string, Record<string, string>> = {
  en: {
    navCatalog: 'Catalog',
    navAdmin: 'Admin',
    navReset: 'System Reset',
    
    // Login
    loginTitle: 'Login to Gila Store',
    loginSubtitle: 'Enter your credentials below',
    usernameLabel: 'Username',
    passwordLabel: 'Password',
    usernameRequired: 'Username is required',
    passwordRequired: 'Password is required',
    loginBtn: 'Login',
    testAccountsTitle: 'Test Accounts',
    adminDemoAccount: 'Admin (admin / password)',
    customerDemoAccount: 'Customer (customer / password)',
    loginFailed: 'Invalid username or password.',
    
    // Catalog
    searchPlaceholder: 'Search products...',
    categoryLabel: 'Category',
    allCategories: 'All Categories',
    cartTitle: 'Shopping Cart',
    buyNowBtn: 'Buy Now',
    catalogEmptyCustomer: 'Welcome! No products in stock. Please contact an administrator to load products.',
    catalogEmptyAdmin: 'No products in stock. Go to Product Manager to load products.',
    goManagerBtn: 'Go to Product Manager',
    noProductsFound: 'No products found matching your criteria.',
    stockLabel: 'Stock:',
    addToCartBtn: 'Add',
    loadMoreBtn: 'Load More Products',
    cartEmptyMessage: 'Your cart is empty.',
    cartTotalLabel: 'Total',
    loginToCheckout: 'to checkout',
    
    // Admin
    unauthorizedTitle: 'Access Denied',
    unauthorizedMsg: 'You do not have administrative permissions to access this control panel.',
    tabCrud: 'Products CRUD',
    tabCsv: 'CSV Bulk Import',
    tabAuditLogs: 'System Audit Logs',
    tabTelemetry: 'Telemetry Analytics',
    adminDashboardTitle: 'Admin Dashboard',
    prodName: 'Name',
    prodCategory: 'Category',
    prodPrice: 'Price',
    prodStock: 'Stock',
    prodDescription: 'Description',
    saveBtn: 'Save Product',
    importTitle: 'Import Products from CSV',
    importInstructions: 'Select a CSV file containing catalog details. Upload will parse and insert records asynchronously.',
    selectFileBtn: 'Select CSV File',
    uploadBtn: 'Upload and Start Import',
    createProductTitle: 'Create New Product',
    editProductTitle: 'Edit Product',
    cancelBtn: 'Cancel',
    tableName: 'Name',
    tableCategory: 'Category',
    tablePrice: 'Price',
    tableStock: 'Stock',
    tableActions: 'Actions',
    importStatusHeader: 'Import Task Progress',
    rowsTotal: 'Total rows found:',
    rowsProcessed: 'Processed rows:',
    rowsErrors: 'Error count:',
    warningsTitle: 'Import Logs & Warnings',
    
    // Checkout Success
    checkoutSuccessTitle: 'Order Confirmed!',
    checkoutSuccessSubtitle: 'Thank you for your purchase. Your payment was processed successfully.',
    continueShopping: 'Continue Shopping',
    orderIdLabel: 'Order ID:',
    orderStatusLabel: 'Status:',
    orderItemsTitle: 'Items Purchased',
    orderTotalLabel: 'Total Paid',
    orderItemQtyLabel: '×',
    noOrderData: 'No order details available.',
    
    // Reset page
    resetTitle: 'Reset System for UAT',
    resetDescription: "This action permanently deletes all orders and restores every product's stock to its initial value. Use this only to reset the application state for evaluator testing.",
    resetBtn: 'Reset All Orders & Restore Stock',
    resetSuccessMsg: 'System reset complete. All orders cleared and stock restored.',
    resetErrorMsg: 'Reset failed. Check server logs.'
  },
  es: {
    navCatalog: 'Catálogo',
    navAdmin: 'Administrador',
    navReset: 'Restablecimiento del Sistema',
    
    // Login
    loginTitle: 'Iniciar sesión en Gila Store',
    loginSubtitle: 'Ingrese sus credenciales a continuación',
    usernameLabel: 'Usuario',
    passwordLabel: 'Contraseña',
    usernameRequired: 'El usuario es obligatorio',
    passwordRequired: 'La contraseña es obligatoria',
    loginBtn: 'Iniciar Sesión',
    testAccountsTitle: 'Cuentas de prueba',
    adminDemoAccount: 'Admin (admin / password)',
    customerDemoAccount: 'Cliente (customer / password)',
    loginFailed: 'Usuario o contraseña inválidos.',
    
    // Catalog
    searchPlaceholder: 'Buscar productos...',
    categoryLabel: 'Categoría',
    allCategories: 'Todas las categorías',
    cartTitle: 'Carrito de compras',
    buyNowBtn: 'Comprar ahora',
    catalogEmptyCustomer: '¡Bienvenido! No hay productos en stock. Comuníquese con un administrador para cargar productos.',
    catalogEmptyAdmin: 'No hay productos en stock. Vaya al Administrador de productos para cargar productos.',
    goManagerBtn: 'Ir al Administrador de productos',
    noProductsFound: 'No se encontraron productos que coincidan con sus criterios.',
    stockLabel: 'Stock:',
    addToCartBtn: 'Agregar',
    loadMoreBtn: 'Cargar más productos',
    cartEmptyMessage: 'Su carrito está vacío.',
    cartTotalLabel: 'Total',
    loginToCheckout: 'para pagar',
    
    // Admin
    unauthorizedTitle: 'Acceso Denegado',
    unauthorizedMsg: 'No tiene permisos administrativos para acceder a este panel de control.',
    tabCrud: 'CRUD de productos',
    tabCsv: 'Importación masiva CSV',
    tabAuditLogs: 'Registros de auditoría del sistema',
    tabTelemetry: 'Análisis de telemetría',
    adminDashboardTitle: 'Panel de administración',
    prodName: 'Nombre',
    prodCategory: 'Categoría',
    prodPrice: 'Precio',
    prodStock: 'Stock',
    prodDescription: 'Descripción',
    saveBtn: 'Guardar producto',
    importTitle: 'Importar productos desde CSV',
    importInstructions: 'Seleccione un archivo CSV que contenga los detalles del catálogo. La carga analizará e insertará los registros de forma asíncrona.',
    selectFileBtn: 'Seleccionar archivo CSV',
    uploadBtn: 'Cargar e iniciar importación',
    createProductTitle: 'Crear nuevo producto',
    editProductTitle: 'Editar producto',
    cancelBtn: 'Cancelar',
    tableName: 'Nombre',
    tableCategory: 'Categoría',
    tablePrice: 'Precio',
    tableStock: 'Stock',
    tableActions: 'Acciones',
    importStatusHeader: 'Progreso de la tarea de importación',
    rowsTotal: 'Total de filas encontradas:',
    rowsProcessed: 'Filas procesadas:',
    rowsErrors: 'Recuento de errores:',
    warningsTitle: 'Registros de importación y advertencias',
    
    // Checkout Success
    checkoutSuccessTitle: '¡Pedido confirmado!',
    checkoutSuccessSubtitle: 'Gracias por su compra. Su pago fue procesado con éxito.',
    continueShopping: 'Continuar comprando',
    orderIdLabel: 'ID del pedido:',
    orderStatusLabel: 'Estado:',
    orderItemsTitle: 'Artículos comprados',
    orderTotalLabel: 'Total pagado',
    orderItemQtyLabel: '×',
    noOrderData: 'No hay detalles del pedido disponibles.',
    
    // Reset page
    resetTitle: 'Restablecer Sistema para Pruebas UAT',
    resetDescription: 'Esta acción elimina permanentemente todas las órdenes y restaura el inventario de cada producto a su valor inicial. Úsela solo para restablecer el estado de la aplicación durante las pruebas del evaluador.',
    resetBtn: 'Eliminar Todas las Órdenes y Restaurar Inventario',
    resetSuccessMsg: 'Restablecimiento completo. Todas las órdenes eliminadas y el inventario restaurado.',
    resetErrorMsg: 'Error al restablecer. Revise los registros del servidor.'
  }
};

@Injectable({
  providedIn: 'root'
})
export class TranslationService {
  public readonly currentLang = signal<'en' | 'es'>('en');

  constructor() {
    if (typeof window !== 'undefined') {
      const stored = localStorage.getItem('gila_lang');
      if (stored === 'en' || stored === 'es') {
        this.currentLang.set(stored);
      }
    }
  }

  public setLanguage(lang: 'en' | 'es'): void {
    this.currentLang.set(lang);
    if (typeof window !== 'undefined') {
      localStorage.setItem('gila_lang', lang);
    }
  }

  public t(key: string): string {
    const lang = this.currentLang();
    const dictionary = TRANSLATIONS[lang] || TRANSLATIONS['en'];
    return dictionary[key] || key;
  }
}
