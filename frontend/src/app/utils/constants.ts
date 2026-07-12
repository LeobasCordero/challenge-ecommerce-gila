export const ERROR_MESSAGES = {
  FETCH_PRODUCTS_FAILED: 'Error loading products. Please try again.',
  FETCH_ADMIN_PRODUCTS_FAILED: 'Error fetching admin products.',
  UPDATE_PRODUCT_FAILED: 'Error updating product. Check log details.',
  CREATE_PRODUCT_FAILED: 'Error creating product. Name must be unique.',
  DELETE_PRODUCT_FAILED: 'Error deleting product.',
  UPLOAD_CSV_FAILED: 'Error uploading file. Check format and rules.',
  IMPORT_FAILED: 'Import task failed.',
  RESET_FAILED: 'Reset failed. Check server logs.',
  LOGIN_FAILED: 'Invalid username or password. Please try again.',
  LOGIN_REQUIRED: 'Please login to add items to your cart.',
  MISSING_PRODUCT_ID: 'Cannot add item: product ID is missing.',
  UPDATE_CART_FAILED: 'Could not update cart.',
  REMOVE_CART_ITEM_FAILED: 'Could not remove item.',
  CHECKOUT_FAILED: 'Checkout failed. Please try again.'
};

export const SUCCESS_MESSAGES = {
  PRODUCT_UPDATED: 'Product updated successfully!',
  PRODUCT_CREATED: 'Product created successfully!',
  PRODUCT_DELETED: 'Product deleted successfully!',
  CSV_UPLOADED: 'CSV file uploaded. Processing import...',
  IMPORT_COMPLETED: 'Bulk import successfully finished!',
  SYSTEM_RESET_COMPLETE: 'System reset complete. All orders cleared and stock restored.',
  ADD_TO_CART: (productName: string) => `${productName} added to cart!`
};

export const MODAL_CONFIRMATIONS = {
  DELETE_PRODUCT: (productName: string) => `Are you sure you want to delete ${productName}?`,
  SYSTEM_RESET: 'This will delete ALL orders and restore all product stock. Continue?'
};
